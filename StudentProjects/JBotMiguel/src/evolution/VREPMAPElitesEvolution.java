package evolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.FloatWAA;
import coppelia.remoteApi;
import mathutils.Vector2d;
import multiobjective.GenericEvaluationFunction;
import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import multiobjective.MOTask;
import novelty.BehaviourResult;
import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.evaluators.FinalPositionWithOrientationBehavior;
import novelty.results.VectorBehaviourExtraResult;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evaluationfunctions.OrientationEvaluationFunction;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.GenerationalEvolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

/**
 *	MAP-Elites "illumination" algorithm
 *	J.-B. Mouret and J. Clune, Illuminating search spaces by mapping elites, arXiv 2015
 * @author miguelduarte
 */
public class VREPMAPElitesEvolution extends MAPElitesEvolution{
	
	private remoteApi vrep;
	private int clientId;
	
    public VREPMAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    	
    	if(!arg.getArgumentIsDefined("genomelength"))
    		throw new RuntimeException("Missing 'genomelength' arg in VREPMapElitesEvolution");
    	
    	vrep = new remoteApi();
		vrep.simxFinish(-1); // just in case, close all opened connections
		clientId = vrep.simxStart("127.0.0.1",19997,true,false,5000,5);
//		System.out.println("Connected to remote API server");
		vrep.simxStartSimulation(clientId,vrep.simx_opmode_oneshot);
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
    
    
    protected float[] getDataFromVREP() {
    	CharWA str=new CharWA(0);
    	while(vrep.simxGetStringSignal(clientId,"toClient",str,remoteApi.simx_opmode_oneshot_wait) != remoteApi.simx_return_ok) {
    		try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    	
		vrep.simxClearStringSignal(clientId, "toClient", remoteApi.simx_opmode_oneshot);
		 
		FloatWA f = new FloatWA(0);
		f.initArrayFromCharArray(str.getArray());
		return f.getArray();
    }
    
    protected void sendDataToVREP(float[] arr) {
    	FloatWA f = new FloatWA(arr.length);
    	f.setValue(arr);
    	char[] chars = f.getCharArrayFromArray();
    	String tempStr = new String(chars);
		CharWA str = new CharWA(tempStr);
		vrep.simxWriteStringStream(clientId,"fromClient",str,remoteApi.simx_opmode_oneshot);
    }
    
    @Override
	protected void evaluateAndAdd(ArrayList<MOChromosome> randomChromosomes) {
    	
    	float currentX = 0;
    	float currentY = 0;
    	
    	try {
			
			float[] arr = new float[randomChromosomes.size()*randomChromosomes.get(0).getAlleles().length];
			int ind = 0;
			for(Chromosome c : randomChromosomes) {
	
				for(int i = 0 ; i < c.getAlleles().length ; i++) {
					arr[ind++] = (float)c.getAlleles()[i];
				}
				print(".");
			}
			
			sendDataToVREP(arr);
			float[] results = getDataFromVREP();
			
			print("\n");
	
			int resultIndex = 0;
			
			for(Chromosome c : randomChromosomes) {
				
				float posX = results[resultIndex++];
				float posY = results[resultIndex++];
				float posZ = results[resultIndex++];
				
				currentX = posX;
				currentY = posY;
				
				float alpha = results[resultIndex++];
				float beta = results[resultIndex++];
				float gamma = results[resultIndex++];
				
				double orientation = beta;
				
				Vector2d pos = new Vector2d(posX,posY);
				
				double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
				FitnessResult fr = new FitnessResult(fitness);
				GenericEvaluationFunction br = new FinalPositionWithOrientationBehavior(new Arguments(""),pos,orientation);
				
				ArrayList<EvaluationResult> sampleResults = new ArrayList<EvaluationResult>();
				int index = 0;
				
				sampleResults.add(index++,fr);
				sampleResults.add(index++,((GenericEvaluationFunction)br).getEvaluationResult());
				
				ExpandedFitness ef = new ExpandedFitness();
				ef.setEvaluationResults(sampleResults);
				
				MOFitnessResult result = new MOFitnessResult((MOChromosome)c, ef);
				MOChromosome moc = result.getChromosome();
				moc.setEvaluationResult(result.getEvaluationResult());
				((MAPElitesPopulation)population).addToMap(moc);
				print("!");
			}
    	}catch (Exception e) {
    		System.out.println(currentX+" "+currentY);
    		e.printStackTrace();
    		System.exit(0);
    	}
	}
    
}