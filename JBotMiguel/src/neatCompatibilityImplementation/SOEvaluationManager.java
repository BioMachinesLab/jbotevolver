package neatCompatibilityImplementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**
 * ENCOG compatible single-objective evaluation function
 * @author miguelduarte42
 *
 */
public class SOEvaluationManager extends SOEvaluation<NEATNetwork> {

	protected int numberOfSamples;
	protected long generationalSeed;
	protected SingleObjective objective;
	protected Arguments objectiveArgs;
	protected int objectiveId = 0;
	protected transient JBotEvolver evolver;
	protected transient TaskExecutor taskExecutor;
	protected final String OBJECTIVE_KEY_PREFIX = "o";
	private boolean supressMessages = false;
	
	private Map<Integer,Stack<SimpleObjectiveResult>> synchronizedMap = Collections.synchronizedMap(new HashMap<Integer,Stack<SimpleObjectiveResult>>());
	private int asked = 0;
	private int getResult = 0;
	private int printed = 0;
	private Lock lock = new ReentrantLock();
	
	public SOEvaluationManager(Arguments args) {
		super(args);
		this.objectiveId = args.getArgumentAsInt("objectiveid");
	}
	
	public void setupObjectives(HashMap<String, Arguments> arguments) {
		int objectiveId = 1;
		Arguments objectiveArgs = arguments.get("--evaluation");
		this.objectiveArgs = objectiveArgs;
		this.objective = (SingleObjective) EvaluationFunction.getEvaluationFunction(objectiveArgs);
		objective.setId(objectiveId);
		objective.setKey(OBJECTIVE_KEY_PREFIX + objectiveId);
		
		Arguments evolutionArgs = arguments.get("--evolution");
		supressMessages = evolutionArgs.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;
	}

	@Override
	public boolean requireSingleThreaded() {
		return false;
	}

	@Override
	public boolean shouldMinimize() {
		return false;
	}

	@Override
	public double calculateScore(MLMethod method) {
		double result = evaluateNetwork((org.encog.neural.neat.NEATNetwork) method);
		return result;
	}

	protected double evaluateNetwork(NEATNetwork net) {
		ArrayList<SimpleObjectiveResult> results = new ArrayList<SimpleObjectiveResult>();
		
		int totalTasksSubmitted = submitTasksForExecution(net);
		int hash = net.hashCode();
		
		//get the results.
		while(totalTasksSubmitted > 0) {
			
			SimpleObjectiveResult result = getResultFromMap(hash);
			if(result != null){
				totalTasksSubmitted--;
				results.add(result);
				print("!");
			} else {
				
				boolean getNewResult = false;
				
				synchronized (this) {
					if(getResult < asked) {
						getNewResult = true;
						getResult++;
					}
				}
				if(getNewResult) {
					result = (SimpleObjectiveResult) taskExecutor.getResult();
					
					if(result.getNetHash() == net.hashCode()) {
						totalTasksSubmitted--;
						results.add(result);
						print("!");
					} else {
						addToMap(result.getNetHash(), result);
					}
				}
			}
		}
				
		//process the results.
		TaskStatistics stats = processTasksResults(results);
		this.statsManager.addControllerStatistics(net, stats);
		
		return stats.get(OBJECTIVE_KEY_PREFIX+1);
	}

	private TaskStatistics processTasksResults(ArrayList<SimpleObjectiveResult> results) {
		TaskStatistics stats = new TaskStatistics(1);
		double fitness = 0;
		
		for(int sample = 0; sample < results.size(); sample++){
			fitness += results.get(sample).getFitness();
		}

		fitness /= results.size();
		
//		objective.setScore(fitness);

		stats.put(OBJECTIVE_KEY_PREFIX+1, fitness);
		
//		System.out.println("fitness: "+fitness);
//		System.out.println(++networks);
		
		return stats;
	}
	
	public String getObjectiveKey(int id){
		if(id > 1)
			return null;
		return this.OBJECTIVE_KEY_PREFIX + id;
	}

	private int submitTasksForExecution(NEATNetwork net) {
		
		int totalTasksSubmitted = 0;
		
		int tasks;
		
		SingleObjective obj = createTaskObjective();
		tasks = createSimulationTasks(obj, net);
		
		totalTasksSubmitted += tasks;
		
		return totalTasksSubmitted;
	}
	
	private SingleObjective createTaskObjective() {
		Arguments objectiveArgs = this.evolver.getArguments().get("--evaluation");
		SingleObjective taskObjective = (SingleObjective) EvaluationFunction.getEvaluationFunction(objectiveArgs);
		taskObjective.setId(objective.getId());
		
		return taskObjective;
	}

	private NEATNetwork createTaskNet(NEATNetwork net) {
		return NEATNetworkController.createCopyNetwork(net);
	}

	private int createSimulationTasks(SingleObjective objective, NEATNetwork net) {
		
		
		for(int i = 0; i < this.numberOfSamples; i++){
			int hash = net.hashCode();
			NEATNetwork taskNet = createTaskNet(net);
			
			this.taskExecutor.addTask(new GenericSimulationGenerationalTask(
					new JBotEvolver(evolver.getArgumentsCopy(),evolver.getRandomSeed()), 
					i, objective, taskNet, 
					generationalSeed+i, hash));
			print(".");
		}
		
		synchronized (this) {
			asked+=numberOfSamples;
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
	public void resetStatistics() {
		this.statsManager.resetStatistics();
	}
	
	@Override
	public double getFitness() {
		if(objective != null)
			return objective.getFitness();
		return 0;
	}
	
	public SimpleObjectiveResult getResultFromMap(int key) {
		
		synchronized (synchronizedMap) {
			
			Stack<SimpleObjectiveResult> s = synchronizedMap.get(key);
			
			if(s == null)
				return null;
			else if(s.isEmpty())
				return null;
			else
				return s.pop();
		}
	}
	
	public void addToMap(int key, SimpleObjectiveResult value) {
		synchronized (synchronizedMap) {
			if (synchronizedMap.containsKey(key)) {
				synchronizedMap.get(key).add(value);
			} else {
				Stack<SimpleObjectiveResult> stack = new Stack<SimpleObjectiveResult>();
				stack.add(value);
				synchronizedMap.put(key, stack);
			}
		}
	}
	
	private void print(String s) {
		synchronized (lock) {
			printed++;
			if(!supressMessages)
				System.out.print(s);
			if(printed%100==0)
				System.out.println();
		}
	}
}