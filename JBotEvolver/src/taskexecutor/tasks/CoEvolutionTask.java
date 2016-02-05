package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;

import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.SimpleCoEvolutionFitnessResult;
import taskexecutor.results.SimpleFitnessResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class CoEvolutionTask extends JBotEvolverTask {

	private int samples;
	private double fitness = 0;
	private Chromosome chromosome;
	private Chromosome opponentChromosome;
	private int numPreys;
	private String evaluation;
	private Random random;

	public CoEvolutionTask(JBotEvolver jBotEvolver, int samples,
			Chromosome chromosome, Chromosome opponentChromosome, int numPreys,
			long seed, String evaluation) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = chromosome;
		this.opponentChromosome = opponentChromosome;
		this.numPreys = numPreys;
		this.evaluation = evaluation;
		this.random = new Random(seed);
		this.jBotEvolver = jBotEvolver;
	}

	@Override
	public void run() {
		
		for (int i = 0; i < samples; i++) {
			
			jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", i);
			long seed = random
					.nextLong();
			
			Simulator simulator = jBotEvolver.createSimulator(new Random(seed));
			simulator.setFileProvider(getFileProvider());

			ArrayList<Robot> robots = jBotEvolver.createCoEvolutionRobots(simulator);
			
			ArrayList<Robot> preys = new ArrayList<Robot>();
			ArrayList<Robot> predators = new ArrayList<Robot>();
			for (int j = 0; j < robots.size(); j++) {
				if (j < numPreys) {
					preys.add(robots.get(j));
				} else {
					predators.add(robots.get(j));
				}
			}
			
			jBotEvolver.setChromosome(preys, chromosome);
			jBotEvolver.setChromosome(predators, opponentChromosome);

			simulator.addRobots(robots);

			EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(jBotEvolver.getArguments().get(evaluation));
			simulator.addCallback(eval);
			simulator.simulate();
			System.out.println(seed+" "+eval.getFitness());
			fitness += eval.getFitness();
		}
	}

	public Result getResult() {
		SimpleCoEvolutionFitnessResult fr = new SimpleCoEvolutionFitnessResult(chromosome.getID(),opponentChromosome.getID(),
				fitness / samples);
		return fr;
	}
}
