package neatCompatibilityImplementation;

import java.util.ArrayList;
import java.util.Random;

import result.Result;

import org.encog.ml.MLMethod;

import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.tasks.JBotEvolverTask;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import extensions.ExtendedJBotEvolver;

public class GenericSimulationGenerationalTask extends JBotEvolverTask {

	private int sampleNumber;
	protected Random random;
	private ExtendedJBotEvolver jBotEvolver;
	protected MLMethod method;
	protected SingleObjective objective;
	protected double fitness = 0;

	public GenericSimulationGenerationalTask(JBotEvolver jBotEvolver, int sampleNumber, 
			SingleObjective objective, MLMethod method, long seed) {
		super(jBotEvolver);
		this.sampleNumber = sampleNumber;
		this.method = method;
		this.jBotEvolver = (ExtendedJBotEvolver) jBotEvolver;
		this.objective = objective;
		this.random = new Random(seed);
	}

	@Override
	public void run() {

		Simulator simulator = jBotEvolver.createSimulator(new Random(random.nextLong()));
		simulator.setFileProvider(getFileProvider());

		jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", sampleNumber);

		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
		//extended jbotevolver specific method
		jBotEvolver.setMLControllers(robots, method);
		simulator.addRobots(robots);

		EvaluationFunction eval = objective.getEvaluationFunction();
		//System.out.println(eval.getClass());
		simulator.addCallback(eval);
		simulator.simulate();
		
		this.fitness = eval.getFitness();
		//System.out.println("fitness: " + this.fitness + "; sample: " + this.sampleNumber);
	}

	@Override
	public Result getResult() {
		/*if(finalScore < 0)
			finalScore = 0;*/
		//System.out.println("RESULT FITNESS: " + this.fitness + "; " + this.sampleNumber);
		SimpleObjectiveResult fr = new SimpleObjectiveResult(objective.getId(), this.sampleNumber, this.fitness);
		return fr;
	}

}
