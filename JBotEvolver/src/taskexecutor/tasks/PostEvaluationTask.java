package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.Random;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.results.SimpleFitnessResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class PostEvaluationTask extends JBotEvolverTask {
	
	private int fitnesssample;
	private int nSamples;
	private double fitness = 0;
	private Chromosome chromosome;
	private JBotEvolver jBotEvolver;
	private double threshold = 0;
	private int run;
	
	public PostEvaluationTask(int run, JBotEvolver jBotEvolver, int fitnesssample, Chromosome chromosome, int nSamples, double threshold) {
		super(jBotEvolver);
		this.fitnesssample = fitnesssample;
		this.chromosome = chromosome;
		this.jBotEvolver = jBotEvolver;
		this.nSamples = nSamples;
		this.threshold = threshold;
		this.run = run;
	}
	
	@Override
	public void run() {
		
		for(int i = 0 ; i < nSamples ; i++) {
			Simulator simulator = jBotEvolver.createSimulator(new Random(i));
			simulator.setFileProvider(getFileProvider());
			
			jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", fitnesssample);
			
			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
			jBotEvolver.setChromosome(robots, chromosome);
			simulator.addRobots(robots);
			
			EvaluationFunction eval = jBotEvolver.getEvaluationFunction();
			simulator.addCallback(eval);
			simulator.simulate();
			if(threshold > 0)
				fitness+= eval.getFitness() > threshold ? 1 : 0;
		}
	}
	public Result getResult() {
		
		double val = threshold > 0 ? fitness : fitness/(double)nSamples;
		
		PostEvaluationResult fr = new PostEvaluationResult(run,fitnesssample,val);
		return fr;
	}
}