package multiobjective;

import java.util.ArrayList;
import java.util.Random;

import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.JBotEvolverTask;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MOTask extends JBotEvolverTask {
	
	private int samples;
	private MOChromosome chromosome;
	private Random random;
	private ArrayList<ExpandedFitness> results;
	
	public MOTask(JBotEvolver jBotEvolver, int samples, Chromosome chromosome, long seed) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = (MOChromosome)chromosome;
		this.random = new Random(seed);
		
		results = new ArrayList<ExpandedFitness>();
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
			
			EvaluationFunction[] eval = jBotEvolver.getEvaluationFunction();
			
			for(EvaluationFunction e : eval)
				simulator.addCallback(e);
			
			simulator.simulate();
			
			ArrayList<EvaluationResult> sampleResults = new ArrayList<EvaluationResult>();
			int index = 0;
			
			for(EvaluationFunction e : eval) {
				if(e instanceof GenericEvaluationFunction) 
					sampleResults.add(index++,((GenericEvaluationFunction)e).getEvaluationResult());
				else
					sampleResults.add(index++,new FitnessResult(e.getFitness()));
			}
			
			ExpandedFitness ef = new ExpandedFitness();
			ef.setEvaluationResults(sampleResults);
			this.results.add(ef);
		}
	}
	public Result getResult() {
		MOFitnessResult fr = new MOFitnessResult(chromosome, ExpandedFitness.setToMeanOf(results));
		return fr;
	}
}