package neatCompatibilityImplementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.encog.ml.MLMethod;
import org.encog.ml.fitness.MultiObjectiveFitness;
import org.encog.neural.neat.NEATNetwork;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;
import extensions.ExtendedJBotEvolver;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;

/**
 * ENCOG compatible Multiobjective evaluation function based on linear scalarisation.
 * @author fernando
 *
 */
/**
 * @author fernando
 *
 */
public class MOEvaluationManager extends MOEvaluation<NEATNetwork> {

	protected int numberOfSamples;
	protected double[] objectivesWeights;

	protected long generationalSeed;
	
	protected MultiObjectiveFitness objectivesFunction;
	protected SingleObjective[] objectives;
	protected transient JBotEvolver evolver;
	protected transient TaskExecutor taskExecutor;
	
	protected MOControllersStatistics<NEATNetwork> statsManager;

	protected final String OBJECTIVE_KEY_PREFIX = "o";
	
	public MOEvaluationManager(Arguments args) {
		super(args);
		this.numberOfObjectives = args.getArgumentAsIntOrSetDefault("numberobjectives", 1);
		//System.out.println("OBJECTIVES " + this.numberOfObjectives);
		this.objectives = new SingleObjective[numberOfObjectives];
		this.objectivesWeights = new double[numberOfObjectives];
		setupWeights(args);

		this.objectivesFunction = new MultiObjectiveFitness();
		
		statsManager = new MOControllersStatistics<NEATNetwork>();
	}
	
	public void setupObjectives(HashMap<String, Arguments> arguments) {
		for(int i = 0; i < this.numberOfObjectives; i++){
			int objectiveId = i + 1;
			
			Arguments objectiveArgs = arguments.get("--objective" + objectiveId);
			SingleObjective objective = (SingleObjective) EvaluationFunction.getEvaluationFunction(objectiveArgs);
			objective.setId(objectiveId);
			objective.setKey(OBJECTIVE_KEY_PREFIX + objectiveId);
			
			this.objectives[i] = objective;
			
			objectivesFunction.addObjective(objectivesWeights[i], objective);
		}
	}

	private void setupWeights(Arguments args) {
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split("-");
			for(int i = 0 ; i < objectivesWeights.length ; i++)
				objectivesWeights[i] = Double.parseDouble(rawArray[i]);
		}
		else {
			for(int i = 0; i < objectivesWeights.length; i++)
				objectivesWeights[i] = 1.0/this.numberOfObjectives;
		}

		//System.out.println("WEIGHTS: " + Arrays.toString(this.objectivesWeights));
	}

	@Override
	public boolean requireSingleThreaded() {
		return true;
	}

	@Override
	public boolean shouldMinimize() {
		return false;
	}

	@Override
	public synchronized double calculateScore(MLMethod method) {
		prepareObjectives((org.encog.neural.neat.NEATNetwork) method);
		return objectivesFunction.calculateScore(method);
	}

	protected synchronized void prepareObjectives(NEATNetwork net) {
		HashMap<Integer, ArrayList<SimpleObjectiveResult>> results = 
				new HashMap<Integer,ArrayList<SimpleObjectiveResult>>(this.numberOfObjectives);
		
		int totalTasksSubmitted = submitTasksForExecution(results, net);
		
		//get the results.
		while(totalTasksSubmitted-- > 0) {
			SimpleObjectiveResult result = (SimpleObjectiveResult) taskExecutor.getResult();
			int objectiveId = result.getResultId();
			results.get(objectiveId).add(result);
		}
		
		//process the results.
		TaskStatistics stats = processTasksResults(results);
		this.statsManager.addControllerStatistics(net, stats);
	}

	private TaskStatistics processTasksResults(
			HashMap<Integer, ArrayList<SimpleObjectiveResult>> results) {
		TaskStatistics stats = new TaskStatistics(this.numberOfObjectives);
		for(int i = 0; i < this.numberOfObjectives; i++){
			SingleObjective objective = this.objectives[i];
			double fitness = 0;
			ArrayList<SimpleObjectiveResult> objectiveResults = results.get(objective.getId());
			
			for(int sample = 0; sample < objectiveResults.size(); sample++){
				fitness += objectiveResults.get(sample).getFitness();
				/*System.out.println("FITNESS: " + objectiveResults.get(sample).getFitness()
						+ "; SAMPLE: " + sample);*/
			}
			//System.out.println();

			fitness /= objectiveResults.size();
			
			objective.setScore(fitness);
		//	System.out.println("objective score @ MOEM: " + fitness);
			stats.put(objective.getKey(), fitness);
		}
		
		return stats;
	}
	
	public String getObjectiveKey(int id){
		if(id > this.numberOfObjectives)
			return null;
		return this.OBJECTIVE_KEY_PREFIX + id;
	}

	private int submitTasksForExecution(
			HashMap<Integer, ArrayList<SimpleObjectiveResult>> results, 
			NEATNetwork net) {
		
		int totalTasksSubmitted = 0;
		
		for(int i = 0; i < this.numberOfObjectives; i++){
			SingleObjective objective = objectives[i];
			int tasks;
			if(objective.requiresSimulation()){
				tasks = createSimulationTasks(objective, net);
			}
			else {
				tasks = createSimpleProcessingTasks(objective, net);
			}
			totalTasksSubmitted += tasks;
			results.put(objective.getId(), new ArrayList<SimpleObjectiveResult>(tasks));
		}
		
		return totalTasksSubmitted;
	}

	
	private int createSimpleProcessingTasks(SingleObjective objective,
			NEATNetwork net) {
		NEATNetwork taskNet = createTaskNet(net);
		SingleObjective taskObjective = createTaskObjective(objective);
		
		this.taskExecutor.addTask(new SimpleProcessingTask(taskObjective, taskNet, evolver.getRandomSeed()));
		
		return 1;
	}

	private SingleObjective createTaskObjective(SingleObjective objective) {
		Arguments objectiveArgs = this.evolver.getArguments().get("--objective" + objective.getId());
		SingleObjective taskObjective = (SingleObjective) 
				EvaluationFunction.getEvaluationFunction(objectiveArgs);
		taskObjective.setId(objective.getId());
		
		return taskObjective;
	}

	private NEATNetwork createTaskNet(NEATNetwork net) {
		return NEATNetworkController.createCopyNetwork(net);
	}

	private int createSimulationTasks(SingleObjective objective,
			NEATNetwork net) {
		for(int i = 0; i < this.numberOfSamples; i++){
			NEATNetwork taskNet = createTaskNet(net);
			SingleObjective taskObjective = createTaskObjective(objective);
			
			this.taskExecutor.addTask(new GenericSimulationGenerationalTask(
					new ExtendedJBotEvolver(evolver.getArgumentsCopy(),evolver.getRandomSeed()), 
					i, taskObjective, taskNet, 
					generationalSeed+i));
		}
		
		return this.numberOfSamples;
	
	}

	public void setupEvolution(JBotEvolver jBotEvolver,
			TaskExecutor taskExecutor, int numberOfSamples, long generationalSeed) {
		this.evolver = jBotEvolver;
		this.taskExecutor = taskExecutor;
		this.numberOfSamples = numberOfSamples;
		this.generationalSeed = generationalSeed;
	}

	@Override
	public Map<NEATNetwork, TaskStatistics> getObjectivesStatistics() {
		return this.statsManager.getObjectivesStatistics();
	}

	@Override
	public void resetStatistics() {
		this.statsManager.resetStatistics();
	}

}
