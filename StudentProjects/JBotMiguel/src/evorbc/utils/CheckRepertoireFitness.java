package evorbc.utils;

import java.io.File;
import java.util.Scanner;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.results.VectorBehaviourExtraResult;
import mathutils.Vector2d;
import multiobjective.MOChromosome;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evorbc.qualitymetrics.CircularQualityMetric;

public class CheckRepertoireFitness {
	
	public static void main(String[] args) throws Exception{
		
//		String f = "repertoire/";
		String f = "bigdisk/december2015/foraging/repertoire/";
		
		System.out.println("Setup\tGeneration\tOrientationFitness\tDistanceFitness\tChromosomes");
		
		for(String s : new File(f).list()) {
			if(new File(f+s).isDirectory()) {
				new CheckRepertoireFitness(f+s);
			}
		}
	}
	
	private int count = 0;
	
	public CheckRepertoireFitness(String folder) throws Exception{
		
		File f = new File(folder);
		
		String result = "";
		
		for(String s : f.list()) {
			result=checkSubFolders(new File(folder+"/"+s+"/"));
			if(!result.isEmpty())
				System.out.println(result);
		}
	}
	
	private String checkSubFolders(File folder) throws Exception {
		
		String result = "";
		
		if(folder.list() == null) {
			return result;
		}
		
		for(String f : folder.list()) {
			
			String fn = folder.getPath()+"/"+f;
			
			if(f.equals("_fitness.log")) {
				result+=checkFitness(folder.getPath()+"/");
			} else if(new File(fn).isDirectory()) {
				result+= checkSubFolders(new File(fn));
			}
		}
		return result;
	}
	
	private String checkFitness(String folder) throws Exception {
		
		StringBuffer result = new StringBuffer();
		
		if(!new File(folder+"_showbest_current.conf").exists() || folder.contains("_20"))
			return result.toString();
		
		int gens = 0;
		
		Scanner s = new Scanner(new File(folder+"_generationnumber"));
		gens = s.nextInt();
		s.close();
		
		String[] split = folder.split("/");
		
		for(int i = 0 ; i <= gens ; i++) {
			JBotEvolver jbot = new JBotEvolver(new String[]{folder+"show_best/showbest"+i+".conf"});
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			
			double distanceFitness = 0;
			double orientationFitness = 0;
			
			for(Chromosome c : pop.getChromosomes()) {
				MOChromosome moc = (MOChromosome)c;
				ExpandedFitness fit = (ExpandedFitness)moc.getEvaluationResult();
				FitnessResult fitRes = (FitnessResult)fit.getCorrespondingEvaluation(0);
				BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
				
				double[] behavior = (double[])br.value();
				Vector2d pos = new Vector2d(behavior[0],behavior[1]);
				double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
				double currentOrientationFitness = CircularQualityMetric.calculateOrientationFitness(pos, orientation); 
				orientationFitness+=currentOrientationFitness;
				distanceFitness+=(fitRes.getFitness()-currentOrientationFitness);
				
			}
			
			distanceFitness/=pop.getChromosomes().length;
			orientationFitness/=pop.getChromosomes().length;
			
			result.append(split[split.length-2]);
			result.append("\t");
			result.append(i);
			result.append("\t");
			result.append(orientationFitness);
			result.append("\t");
			result.append(distanceFitness);
			result.append("\t");
			result.append(pop.getChromosomes().length+"\n");
		}
		
		return result.toString();
	}
	
	private void print() {
		if(count++ % 50 == 0)
			System.out.println();
		System.out.print(".");
	}

}
