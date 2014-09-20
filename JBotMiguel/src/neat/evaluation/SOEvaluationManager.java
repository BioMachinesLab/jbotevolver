package neat.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import neat.NEATGenerationalTask;
import neat.NEATNetworkController;
import neat.SimpleObjectiveResult;
import neat.continuous.NEATContinuousNetwork;
import neat.continuous.NEATContinuousNetworkController;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;

/**
 * ENCOG compatible single-objective evaluation function
 * 
 * @author miguelduarte42
 */
public class SOEvaluationManager extends SOEvaluation<NEATNetwork> {

	protected int numberOfSamples;
	protected long generationalSeed;
	protected Arguments objectiveArgs;
	protected int objectiveId = 0;
	protected transient JBotEvolver evolver;
	protected transient TaskExecutor taskExecutor;
	protected final String OBJECTIVE_KEY_PREFIX = "o";
	private boolean supressMessages = false;
	
	private Map<Long,Stack<SimpleObjectiveResult>> synchronizedMap = Collections.synchronizedMap(new HashMap<Long,Stack<SimpleObjectiveResult>>());
	private int asked = 0;
	private int getResult = 0;
	private Lock lock = new ReentrantLock();
	
	private HashMap<Integer, MLMethod> networksEvaluated = new HashMap<Integer, MLMethod>();
	
	public SOEvaluationManager(Arguments args) {
		super(args);
		this.objectiveId = args.getArgumentAsInt("objectiveid");
	}
	
	public void setupObjectives(HashMap<String, Arguments> arguments) {
		Arguments objectiveArgs = arguments.get("--evaluation");
		this.objectiveArgs = objectiveArgs;
		
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
		double result = evaluateNetwork(method);
		return result;
	}
	
	@Override
	public void submitEvaluation(MLMethod method, int evalId) {
		submitTasksForExecution(method, evalId, false);
		networksEvaluated.put(evalId, method);
	}
	
	@Override
	public EvaluationResult getEvaluationResult() {
		SimpleObjectiveResult result = (SimpleObjectiveResult) taskExecutor.getResult();
		print("!");
		
		double fitness = result.getFitness();
		fitness = ((int)(fitness*100)/100.0);
		
		EvaluationResult er = new EvaluationResult((int)result.getThreadId(), fitness);
		ArrayList<SimpleObjectiveResult> results = new ArrayList<SimpleObjectiveResult>();
		results.add(result);
		TaskStatistics stats = processTasksResults(results);
		
		MLMethod net = networksEvaluated.get(er.getEvalId());
		networksEvaluated.remove(er.getEvalId());
		
		this.statsManager.addControllerStatistics(net, stats);
		return er;
	}

	protected double evaluateNetwork(MLMethod net) {
		ArrayList<SimpleObjectiveResult> results = new ArrayList<SimpleObjectiveResult>();
		
		int totalTasksSubmitted = submitTasksForExecution(net, 0, true);
		long threadId = Thread.currentThread().getId();
		
		//get the results
		while(totalTasksSubmitted > 0) {
			
			SimpleObjectiveResult result = getResultFromMap(threadId);
			
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
					if(result.getThreadId() == threadId) {
						totalTasksSubmitted--;
						results.add(result);
						print("!");
					} else {
						addToMap(result.getThreadId(), result);
					}
				}
			}
		}
				
		//process the results
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
		
		fitness /= (double)results.size();
		fitness = ((int)(fitness*100)/100.0);
//		objective.setScore(fitness);

		stats.put(OBJECTIVE_KEY_PREFIX+1, fitness);
		return stats;
	}
	
	public String getObjectiveKey(int id){
		if(id > 1)
			return null;
		return this.OBJECTIVE_KEY_PREFIX + id;
	}

	private int submitTasksForExecution(MLMethod net, int evalId, boolean useThreadId) {
		
		Arguments objectiveArgs = getObjectiveArgs();
		int separateSamples = 1;
		int aggregateSamples = numberOfSamples;
		
		long threadId = Thread.currentThread().getId();
		
		for(int i = 0; i < separateSamples; i++){
			
			this.taskExecutor.addTask(new NEATGenerationalTask(new JBotEvolver(evolver.getArgumentsCopy(),evolver.getRandomSeed()), i, objectiveArgs, net, generationalSeed+i, useThreadId ? threadId : evalId, aggregateSamples));
			print(".");
		}
		
		synchronized (this) {
			asked+= separateSamples;
		}
		
		return separateSamples;
	}
	
	private Arguments getObjectiveArgs() {
		Arguments objectiveArgs = this.evolver.getArguments().get("--evaluation");
		return objectiveArgs;
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
//		if(objective != null)
//			return objective.getFitness();
		return 0;
	}
	
	public SimpleObjectiveResult getResultFromMap(long key) {
		
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
	
	public void addToMap(long key, SimpleObjectiveResult value) {
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
			if(!supressMessages)
				System.out.print(s);
		}
	}
}