package taskexecutor.tasks;

import java.util.ArrayList;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.PostEvaluationResult;

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
		this.nSamples = nSamples;
		this.threshold = threshold;
		this.run = run;
	}
	
	@Override
	public void run() {
		
		for(int i = 0 ; i < nSamples ; i++) {
			Simulator simulator = jBotEvolver.createSimulator(i);
			simulator.setFileProvider(getFileProvider());
			
			jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", fitnesssample);
			
//			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
//			jBotEvolver.setChromosome(robots, chromosome);
			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
			simulator.addRobots(robots);
			
			EvaluationFunction eval = jBotEvolver.getEvaluationFunction()[0];
			simulator.addCallback(eval);
			simulator.simulate();
			System.out.println(i+" "+eval.getFitness());
			if(threshold > 0)
				fitness+= eval.getFitness() > threshold ? 1 : 0;
			else
				fitness+= eval.getFitness();	
		}
	}
	@Override
	public Result getResult() {
		
		double val = threshold > 0 ? fitness : fitness/nSamples;
		
		PostEvaluationResult fr = new PostEvaluationResult(getId(),run,fitnesssample,val);
		return fr;
	}
}