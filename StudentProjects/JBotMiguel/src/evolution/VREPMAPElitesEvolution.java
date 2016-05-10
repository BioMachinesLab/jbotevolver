package evolution;

import java.util.ArrayList;

import mathutils.Vector2d;
import multiobjective.GenericEvaluationFunction;
import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import novelty.BehaviourResult;
import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.evaluators.FinalPositionWithOrientationBehavior;
import novelty.results.VectorBehaviourExtraResult;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.VREPResult;
import taskexecutor.VREPTask;
import taskexecutor.VREPTaskExecutor;
import coppelia.remoteApi;
import evaluationfunctions.OrientationEvaluationFunction;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;

/**
 *	MAP-Elites "illumination" algorithm
 *	J.-B. Mouret and J. Clune, Illuminating search spaces by mapping elites, arXiv 2015
 * @author miguelduarte
 */
public class VREPMAPElitesEvolution extends MAPElitesEvolution{
	
    public VREPMAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    	
    	if(!arg.getArgumentIsDefined("genomelength"))
    		throw new RuntimeException("Missing 'genomelength' arg in VREPMapElitesEvolution");
    }
    
    public static double getFitness(MOChromosome moc) {
    	ExpandedFitness fit = (ExpandedFitness)moc.getEvaluationResult();
		BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
		
		double[] behavior = (double[])br.value();
		Vector2d pos = new Vector2d(behavior[0],behavior[1]);
		double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
		double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
		return fitness;
    }
    
    
    @Override
	protected void evaluateAndAdd(ArrayList<MOChromosome> randomChromosomes) {
    	
    	float currentX = 0;
    	float currentY = 0;
    	
    	try {
			
			VREPTaskExecutor te = (VREPTaskExecutor)taskExecutor;
			int instances = te.getInstances();
			int chromosomesPerInstance = randomChromosomes.size()/instances;
			
			float fixedParameters[] = new float[1+1];
			fixedParameters[0] = 1; //size
			fixedParameters[1] = 3; //seconds of evaluation
			
			int totalIndex = 0;
			
			for(int i = 0 ; i < instances ; i++){
				
				float[] parameters = new float[fixedParameters.length + 1 + (chromosomesPerInstance*(2+randomChromosomes.get(0).getAlleles().length))];
				
				int index = 0;
				
				for(int fp = 0 ; fp < fixedParameters.length ; fp++)
					parameters[index++] = fixedParameters[fp];
				
				parameters[index++] = chromosomesPerInstance; //number of individuals
				
				for(int ci = 0 ; ci < chromosomesPerInstance ; ci++) {
					Chromosome c = randomChromosomes.get(totalIndex++);
					
					parameters[index++] = c.getID(); //id of the chromosome
					parameters[index++] = c.getAlleles().length; //lenght of chromosome
					
					for(int j = 0 ; j < c.getAlleles().length ; j++) {
						parameters[index++] = (float)c.getAlleles()[j];
					}
					print(".");
				}
				VREPTask task = new VREPTask(parameters);
				taskExecutor.addTask(task);
				print("\n");
			}
			
			for(int i = 0 ; i < instances ; i++){
				VREPResult res = (VREPResult) taskExecutor.getResult();
				
				float[] vals = res.getValues();
				
				int index = 0;
				
				int nResults = (int)vals[index++];
				int nVals = (int)vals[index++];
				
				for(int r = 0 ; r < nResults ; r++) {
					int id = (int)vals[index++];
					
					Chromosome c = null;
					
					for(int ch = 0 ; ch < randomChromosomes.size() ; ch++) {
						if(randomChromosomes.get(ch).getID() == id) {
							c = randomChromosomes.get(ch);
							break;
						}
					}
					
					float posX = vals[index++];
					float posY = vals[index++];
					float posZ = vals[index++];
					
					currentX = posX;
					currentY = posY;
					
					float orX = vals[index++];
					float orY = vals[index++];
					float orZ = vals[index++];
					
					double orientation = Math.atan2(orY,orX);
					
					Vector2d pos = new Vector2d(posX,posY);
					
					double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
					FitnessResult fr = new FitnessResult(fitness);
					GenericEvaluationFunction br = new FinalPositionWithOrientationBehavior(new Arguments(""),pos,orientation);
					
					ArrayList<EvaluationResult> sampleResults = new ArrayList<EvaluationResult>();
					int sampleIndex = 0;
					
					sampleResults.add(sampleIndex++,fr);
					sampleResults.add(sampleIndex++,((GenericEvaluationFunction)br).getEvaluationResult());
					
					ExpandedFitness ef = new ExpandedFitness();
					ef.setEvaluationResults(sampleResults);
					
					MOFitnessResult result = new MOFitnessResult((MOChromosome)c, ef);
					MOChromosome moc = result.getChromosome();
					moc.setEvaluationResult(result.getEvaluationResult());
					((MAPElitesPopulation)population).addToMap(moc);
					print("!");
				}
				print("\n");
			}
			
    	}catch (Exception e) {
    		System.out.println(currentX+" "+currentY);
    		e.printStackTrace();
    		System.exit(0);
    	}
	}
}