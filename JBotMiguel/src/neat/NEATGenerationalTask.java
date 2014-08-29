package neat;

import java.util.ArrayList;
import java.util.Random;

import result.Result;
import neat.evaluation.SingleObjective;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.tasks.JBotEvolverTask;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class NEATGenerationalTask extends JBotEvolverTask {

	private int sampleNumber;
	protected Random random;
	private JBotEvolver jBotEvolver;
	protected MLMethod method;
	protected SingleObjective objective;
	protected double fitness = 0;
	protected int netHash;

	public NEATGenerationalTask(JBotEvolver jBotEvolver, int sampleNumber, 
			SingleObjective objective, MLMethod method, long seed, int hash) {
		super(jBotEvolver);
		this.sampleNumber = sampleNumber;
		this.method = method;
		this.jBotEvolver = jBotEvolver;
		this.objective = objective;
		this.random = new Random(seed);
		this.netHash = hash;
	}

	@Override
	public void run() {

		Simulator simulator = jBotEvolver.createSimulator(new Random(random.nextLong()));
		simulator.setFileProvider(getFileProvider());

		jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", sampleNumber);

		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
		//extended jbotevolver specific method
		setMLControllers(robots, method);
		simulator.addRobots(robots);

		EvaluationFunction eval = objective.getEvaluationFunction();
		//System.out.println(eval.getClass());
		simulator.addCallback(eval);
		simulator.simulate();
		
		this.fitness = eval.getFitness();
	}
	
	private void setMLControllers(ArrayList<Robot> robots, MLMethod method) {
		for(Robot r : robots) {
			NEATNetworkController controller = (NEATNetworkController) r.getController();
			NEATNetwork network = (NEATNetwork) method;
			controller.setNetwork(network);
		}
	}

	@Override
	public Result getResult() {
		/*if(finalScore < 0)
			finalScore = 0;*/
		//System.out.println("RESULT FITNESS: " + this.fitness + "; " + this.sampleNumber);
		SimpleObjectiveResult fr = new SimpleObjectiveResult(objective.getId(), this.sampleNumber, this.fitness, this.netHash);
		return fr;
	}
}