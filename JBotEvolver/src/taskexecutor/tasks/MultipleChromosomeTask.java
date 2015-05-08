package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;

import result.Result;
import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.robot.Robot;
import taskexecutor.results.SimpleFitnessResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.JoinedGenerationalEvolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.neuralnetworks.MultipleChromosome;
import gui.ResultViewerGui.SimulationRunner;

public class MultipleChromosomeTask extends JBotEvolverTask{

	private int samples;
	private double fitness = 0;
	private MultipleChromosome chromosome;
	private Random random;
	private long seed;

	public MultipleChromosomeTask(JBotEvolver jBotEvolver, int samples, MultipleChromosome chromosome, long seed) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = chromosome;
		this.random = new Random(seed);
		this.seed = seed;
	}

	@Override
	public void run() {

		for(int i = 0 ; i < samples ; i++) {

			jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", i);
			Simulator simulator = jBotEvolver.createSimulator(new Random(random.nextLong()));
			simulator.setFileProvider(getFileProvider());

			//for(int j = 0; j < JoinedGenerationalEvolution.CHROM_NUM; j++){
			//				ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
			//				jBotEvolver.setChromosome(robots, chromosome.getChromosome(j));
			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
			//System.out.println("Number of Robots: " + robots.size() + " RandomSeed: " + seed);
			simulator.addRobots(robots);
			//}

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
