package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.parallel.SlaveResult;

public class GenerationalTask extends JBotEvolverTask {
	
	private int samples;
	private double fitness = 0;
	private Chromosome chromosome;
	private Random random;
	private JBotEvolver jBotEvolver;
	
	public GenerationalTask(JBotEvolver jBotEvolver, int samples, Chromosome chromosome, int seed) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = chromosome;
		this.random = new Random(seed);
		this.jBotEvolver = jBotEvolver;
	}
	
	@Override
	public void run() {
		for(int i = 0 ; i < samples ; i++) {
			Simulator simulator = jBotEvolver.createSimulator(new Random(random.nextLong()));
			
			jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", i);
			
			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
			jBotEvolver.setChromosome(robots, chromosome);
			simulator.addRobots(robots);
			
			EvaluationFunction eval = jBotEvolver.getEvaluationFunction(simulator);
			simulator.addCallback(eval);
			simulator.simulate();
			
			fitness = eval.getFitness();
		}
	}
	public Result getResult() {
		SlaveResult sr = new SlaveResult(chromosome.getID(),fitness);
		return sr;
	}
}