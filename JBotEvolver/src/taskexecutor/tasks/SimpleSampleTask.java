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

public class SimpleSampleTask extends JBotEvolverTask {
	
	private int fitnesssample;
	private double fitness = 0;
	private Chromosome chromosome;
	private Random random;
	private JBotEvolver jBotEvolver;
	
	public SimpleSampleTask(JBotEvolver jBotEvolver, int fitnesssample, Chromosome chromosome, int seed) {
		super(jBotEvolver);
		this.fitnesssample = fitnesssample;
		this.chromosome = chromosome;
		this.random = new Random(seed);
		this.jBotEvolver = jBotEvolver;
	}
	
	@Override
	public void run() {
		Simulator simulator = jBotEvolver.createSimulator(random);
		
		jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", fitnesssample);
		
		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
		jBotEvolver.setChromosome(robots, chromosome);
		simulator.addRobots(robots);
		
		EvaluationFunction eval = jBotEvolver.getEvaluationFunction();
		simulator.addCallback(eval);
		simulator.simulate();
		
		fitness= eval.getFitness();
	}
	public Result getResult() {
		SimpleFitnessResult fr = new SimpleFitnessResult(chromosome.getID(),fitness);
		return fr;
	}
}