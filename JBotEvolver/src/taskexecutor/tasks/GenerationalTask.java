package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.SimpleFitnessResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class GenerationalTask extends JBotEvolverTask {
	
	private int samples;
	private double fitness = 0;
	private Chromosome chromosome;
	private Random random;
	
	public GenerationalTask(JBotEvolver jBotEvolver, int samples, Chromosome chromosome, long seed) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = chromosome;
		this.random = new Random(seed);
	}
	
	@Override
	public void run() {
		
		for(int i = 0 ; i < samples ; i++) {
			
			jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", i);
			Simulator simulator = jBotEvolver.createSimulator(new Random(random.nextLong()));
			simulator.setFileProvider(getFileProvider());
			
//			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
//			jBotEvolver.setChromosome(robots, chromosome);
			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
			simulator.addRobots(robots);
			
			EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(jBotEvolver.getArguments().get("--evaluation"));
			simulator.addCallback(eval);
			simulator.simulate();
			
			fitness+= eval.getFitness();
		}
	}
	public Result getResult() {
		SimpleFitnessResult fr = new SimpleFitnessResult(chromosome.getID(),fitness/samples);
		return fr;
	}
}