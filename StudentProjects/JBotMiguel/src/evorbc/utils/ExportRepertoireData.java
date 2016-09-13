package evorbc.utils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.results.VectorBehaviourExtraResult;
import simulation.util.Arguments;
import utils.TraverseFolders;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evorbc.qualitymetrics.CircularQualityMetric;
import evorbc.qualitymetrics.RadialQualityMetric;

public class ExportRepertoireData extends TraverseFolders {
	
	private static String FOLDER_NAME = "export";
	
	private String fileName;
	private FileWriter fw;
	
	private ArrayList<String> symbolicLinks = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception{
		
//		new ExportRepertoireData("bigdisk/","repertoire-data-w1.txt").traverse();
		new ExportRepertoireData("bigdisk2/","repertoire-data-w2.txt").traverse();
	}
	
	public ExportRepertoireData(String folder, String fileName) throws Exception{
		super(folder);
		this.fileName = fileName;
	}
	
	protected void traverseStarted() {
		System.out.println(baseFolder+" started!");
		try {
			new File(FOLDER_NAME).mkdir();
			new File(FOLDER_NAME+"/"+fileName).delete();
			fw = new FileWriter(new File(FOLDER_NAME+"/"+fileName));
			fw.append("Folder\tQuality\tTime\tBinSize\tRobot\tRun\tChromosomeId\tBinX\tBinY\tX\tY\tOrientationFitness\tDistanceFitness\tAllele\n");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void traverseEnded() {
		try {
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(baseFolder+" ended!");
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		for(String s : symbolicLinks) {
			if (folder.getPath().contains(s+"/")) {
				System.out.println("link: "+folder.getPath());
				return false;
			}
		}
		
		if(Files.isSymbolicLink(folder.toPath())) {
			symbolicLinks.add(folder.getPath());
			System.out.println("link: "+folder.getPath());
			return false;
		}

		if(new File(folder.getPath()+"/repertoire_name.txt").exists())
			return true;

		return false;
	}
	
	protected void act(File f) {
		
		try {
		
			String folder = f.getPath();
			System.out.println(folder);
			
			String[] split = folder.split("/");
			
			String baseFolder = "";
			
			for(int i = 0 ; i < split.length-3 ; i++ ) {
				baseFolder+=split[i]+"/";
			}
			
			String time = "0";
			String quality = "";
			String binsize = "0";
			String robot = "";
			
			JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf","--simulator","+folder="+baseFolder});
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			
			EvaluationFunction[] functions = jbot.getEvaluationFunction();
			quality = functions[0].getClass().getSimpleName().replace("QualityMetric", "");
			if(functions[0].getArgs().getFlagIsTrue("distance"))
				quality+="+Distance";
			
			binsize = pop.getMapResolution()*100+"";
			
			time = (jbot.getArguments().get("--environment").getArgumentAsDouble("steps")/10.0)+"";
			
			robot = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators")).getArgumentAt(0).replace("Actuator", "").replace("_", "-").replace("A", "4").replace("F", "2");
			
			String runNumber = split[split.length-1];
			
			String prefix = folder+"\t"+
					quality+"\t"+
					time+"\t"+
					binsize+"\t"+
					robot+"\t"+
					runNumber;
		
			StringBuffer result = new StringBuffer();
			
			for(Chromosome c : pop.getChromosomes()) {
				MOChromosome moc = (MOChromosome)c;
				ExpandedFitness fit = (ExpandedFitness)moc.getEvaluationResult();
				FitnessResult fitRes = (FitnessResult)fit.getCorrespondingEvaluation(0);
				BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
				
				double[] behavior = (double[])br.value();
				Vector2d pos = new Vector2d(behavior[0],behavior[1]);
				double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
				
				double orientationFitness = 0;
				double distanceFitness = 0;
				
				int[] loc = pop.getLocationFromBehaviorVector(behavior);
				
				if(functions[0] instanceof CircularQualityMetric) {
					orientationFitness = CircularQualityMetric.calculateOrientationFitness(pos, orientation);
				} else if(functions[0] instanceof RadialQualityMetric) {
					orientationFitness = RadialQualityMetric.calculateOrientationFitness(pos, orientation);
				}
				
				distanceFitness+=(fitRes.getFitness()-orientationFitness);
	
				result.append(prefix).append("\t")
					.append(c.getID()).append("\t")
					.append(loc[0]).append("\t")
					.append(loc[1]).append("\t")
					.append(behavior[0]).append("\t")
					.append(behavior[1]).append("\t")
					.append(orientationFitness).append("\t")
					.append(distanceFitness);
				
				for(int i = 0 ; i < moc.getAlleles().length ; i++) {
					result.append("\t").append(moc.getAlleles()[i]);
				}
				
				result.append("\n");
			}
			
			fw.append(result);
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
