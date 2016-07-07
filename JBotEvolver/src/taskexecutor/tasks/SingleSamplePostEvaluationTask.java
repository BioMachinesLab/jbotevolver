package taskexecutor.tasks;

import java.util.ArrayList;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.PostEvaluationResult;

public class SingleSamplePostEvaluationTask extends JBotEvolverTask {
	
	private int fitnesssample;
	private int sample;
	private double fitness = 0;
	private Chromosome chromosome;
	private double threshold = 0;
	private int run;
	
	public SingleSamplePostEvaluationTask(int run, JBotEvolver jBotEvolver, int fitnesssample, Chromosome chromosome, int sample, double threshold) {
		super(jBotEvolver);
		this.fitnesssample = fitnesssample;
		this.chromosome = chromosome;
		this.sample = sample;
		this.threshold = threshold;
		this.run = run;
	}
	
	@Override
	public void run() {
		jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", fitnesssample);
		
		Simulator simulator = jBotEvolver.createSimulator(sample);
		simulator.setFileProvider(getFileProvider());
		//TODO comment this line
//		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator); 
//		jBotEvolver.setChromosome(robots, chromosome);
		//TODO uncomment this line
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
	@Override
	public Result getResult() {
		PostEvaluationResult fr = new PostEvaluationResult(getId(),run,fitnesssample,fitness,sample);
		return fr;
	}
}