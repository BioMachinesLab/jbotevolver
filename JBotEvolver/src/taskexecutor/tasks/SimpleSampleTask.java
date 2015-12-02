package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.PostEvaluationResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class SimpleSampleTask extends JBotEvolverTask {
	
	private int fitnesssample;
	private double fitness = 0;
	private Chromosome chromosome;
	private int run;
	private long randomSeed;
	
	public SimpleSampleTask(int run, JBotEvolver jBotEvolver, int fitnesssample, Chromosome chromosome, long randomSeed) {
		super(jBotEvolver);
		this.fitnesssample = fitnesssample;
		this.chromosome = chromosome;
		this.run = run;
		this.randomSeed = randomSeed;
	}
	
	@Override
	public void run() {
		
		jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", fitnesssample);
		
		Simulator simulator = jBotEvolver.createSimulator(new Random(randomSeed));
		simulator.setFileProvider(getFileProvider());
		
//		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
//		jBotEvolver.setChromosome(robots, chromosome);
		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
		simulator.addRobots(robots);
		
		EvaluationFunction eval = jBotEvolver.getEvaluationFunction()[0];
		simulator.addCallback(eval);
		simulator.simulate();
		fitness = eval.getFitness();	
	}
	
	public Result getResult() {
		PostEvaluationResult fr = new PostEvaluationResult(run,fitnesssample,fitness);
		return fr;
	}
}