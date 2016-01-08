package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;

import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.NEATPostEvaluationResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class NEATSingleSamplePostEvaluationTask extends JBotEvolverTask {

	private int fitnesssample;
	private int sample;
	private double fitness = 0;
	private Chromosome chromosome;
	private double threshold = 0;
	private int run;
	private int generation;
	
	public NEATSingleSamplePostEvaluationTask(int run, int generation, JBotEvolver jBotEvolver, int fitnesssample, Chromosome chromosome, int sample, double threshold) {
		super(jBotEvolver);
		this.fitnesssample = fitnesssample;
		this.chromosome = chromosome;
		this.sample = sample;
		this.threshold = threshold;
		this.run = run;
		this.generation = generation;
	}
	
	@Override
	public void run() {
		jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", fitnesssample);
		
		Simulator simulator = jBotEvolver.createSimulator(new Random(sample));
		simulator.setFileProvider(getFileProvider());
//		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
//		jBotEvolver.setChromosome(robots, chromosome);
		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
		simulator.addRobots(robots);
		EvaluationFunction eval = jBotEvolver.getEvaluationFunction()[0];
		simulator.addCallback(eval);
		simulator.simulate();
		if(threshold > 0)
			fitness= eval.getFitness() >= threshold ? 1 : 0;
		else
			fitness= eval.getFitness();
	}
	public Result getResult() {
		NEATPostEvaluationResult fr = new NEATPostEvaluationResult(run,generation,fitnesssample,fitness,sample);
		return fr;
	}
	
}
