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
	private Chromosome chromosome;
	private Random random;
	private ExpandedFitness[] results;
	
	public MOTask(JBotEvolver jBotEvolver, int samples, Chromosome chromosome, long seed) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = chromosome;
		this.random = new Random(seed);
		
		results = new ExpandedFitness[samples];
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
			
			EvaluationResult[] sampleResults = new EvaluationResult[eval.length];
			int index = 0;
			
			for(EvaluationFunction e : eval) {
				if(e instanceof GenericEvaluationFunction) 
					sampleResults[index++] = ((GenericEvaluationFunction)e).getEvaluationResult();
				else
					sampleResults[index++] = new FitnessResult(e.getFitness());
			}
			
			ExpandedFitness ef = new ExpandedFitness();
			ef.setEvaluationResults(sampleResults);
			this.results[i] = ef;
		}
	}
	public Result getResult() {
		MOFitnessResult fr = new MOFitnessResult(chromosome.getID(), ExpandedFitness.setToMeanOf(results));
		return fr;
	}
}