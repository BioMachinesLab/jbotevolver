package vrep;

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
import evolution.MAPElitesEvolution;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;

/**
 *	MAP-Elites "illumination" algorithm
 *	J.-B. Mouret and J. Clune, Illuminating search spaces by mapping elites, arXiv 2015
 * @author miguelduarte
 */
public class VREPMAPElitesEvolution extends MAPElitesEvolution{
	
	protected int controllerType = 0;
	protected int time = 0;
	public int excluded = 0;
	protected double tiltThreshold = 0;
	
    public VREPMAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    	
    	if(!arg.getArgumentIsDefined("genomelength"))
    		throw new RuntimeException("Missing 'genomelength' arg in VREPMapElitesEvolution");
    	
    	if(!arg.getArgumentIsDefined("time"))
    		throw new RuntimeException("Missing 'time' arg in VREPMapElitesEvolution");
    	
    	controllerType = arg.getArgumentAsIntOrSetDefault("controllertype", controllerType);
    	time = arg.getArgumentAsIntOrSetDefault("time", time);
    	tiltThreshold = Math.toRadians(arg.getArgumentAsDoubleOrSetDefault("tiltthreshold", tiltThreshold));
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
    	
    	try {
    		Chromosome[] chromosomes = new Chromosome[randomChromosomes.size()];
    		
    		for(int i = 0 ; i < chromosomes.length ; i++)
    			chromosomes[i] = randomChromosomes.get(i);
    		
    		float fixedParameters[] = new float[1+1];
			fixedParameters[0] = 1; //size
			fixedParameters[1] = time; //seconds of evaluation
    		
			int controllerType = 0;
			
    		int nTasks = VRepUtils.sendTasks((VREPTaskExecutor)taskExecutor, chromosomes, fixedParameters, controllerType);
    		
			float[][] results = VRepUtils.receiveTasks((VREPTaskExecutor)taskExecutor, nTasks);
			
			for(int t = 0 ; t < results.length ; t++) {
				
				float[] vals = results[t];
				
				int index = 0;
				
				int nResults = (int)vals[index++];
				
				for(int r = 0 ; r < nResults ; r++) {
					int id = (int)vals[index++];
					
					Chromosome c = null;
					
					for(int ch = 0 ; ch < randomChromosomes.size() ; ch++) {
						if(randomChromosomes.get(ch).getID() == id) {
							c = randomChromosomes.get(ch);
							break;
						}
					}
					
					int nVals = (int)vals[index++];
					
					//positions of robot
					float posX = vals[index++];
					float posY = vals[index++];
					float posZ = vals[index++];
					
					//euler angles of robot
					float a = vals[index++];
					float b = vals[index++];
					float g = vals[index++];
					
					//distance
					float distanceTraveled = vals[index++];
					
					float minTilt = vals[index++];
					
					double orY = (Math.cos(a)*Math.cos(g) - Math.sin(a)*Math.sin(b)*Math.sin(g));
					double orX = - Math.cos(b)*Math.sin(g);
					double orZ = (Math.cos(g)*Math.sin(a) - Math.cos(a)*Math.sin(b)*Math.sin(b));
					
					double orientation = Math.atan2(orY,orX);
					double tilt = Math.cos(a)*Math.cos(b);
					
					Vector2d pos = new Vector2d(posX,posY);
					
					double distanceLimit = ((MAPElitesPopulation)population).getDistanceLimit()/2;
					
					double fitness = OrientationEvaluationFunction.calculateOrientationDistanceFitness(pos, orientation, distanceTraveled, distanceLimit);
					
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
					
					if(minTilt > tiltThreshold) {
						((MAPElitesPopulation)population).addToMap(moc);
					} else {
						excluded++;
					}
				}
			}
			System.out.println("Total excluded so far: "+excluded);
			
    	}catch (Exception e) {
    		e.printStackTrace();
    		System.exit(0);
    	}
	}
}