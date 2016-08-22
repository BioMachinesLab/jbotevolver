package utils.evorbc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import simulation.Simulator;
import utils.TraverseFolders;
import evaluationfunctions.OrientationEvaluationFunction;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import fourwheeledrobot.MultipleWheelRepertoireActuator;

public class EvoRBCOrientationFitness extends TraverseFolders{
	
	private static String FOLDER_NAME = "export";
	
	private String fileName;
	private FileWriter fw;
	private LinkedList<String> setupNames = new LinkedList<String>();
	
	public EvoRBCOrientationFitness(String baseFolder, String[] setups, String fileName) throws IOException {
		super(baseFolder, setups);
		this.fileName = fileName;
	}
	
	public static void main(String[] args) throws Exception{
		
		EvoRBCOrientationFitness orientationError = null;
		orientationError = new EvoRBCOrientationFitness("orico/repertoireresolution/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_50/","maze_quality_100/","maze_quality_200/"}, "repertoireresolution.txt");
		orientationError.traverse(); System.out.println("Done "+orientationError.fileName);
	}
	
	@Override
	protected void traverseStarted() {
		
		try {
		
			new File(FOLDER_NAME).mkdir();
			new File(FOLDER_NAME+"/orientation-fitness-"+this.fileName).delete();
			fw = new FileWriter(new File(FOLDER_NAME+"/orientation-fitness-"+this.fileName));
			
			fw.append("Folder Setup Fitness\n");
		
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
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		if(new File(folder.getPath()+"/repertoire_name.txt").exists())
			return false;
		
		if(new File(folder.getPath()+"/_showbest_current.conf").exists())
			return true;
		
		return false;
	}
	
	protected void act(File folder) {
		
		try {
			getOrientationError(folder.getPath());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getOrientationError(String folder) throws Exception {
		
		String[] split = folder.split("/");
		
		String folderName = split[split.length-4];
		String setupName = split[split.length-2];
		
		if(!setupNames.contains(setupName+"_"+split[split.length-3])) {
			setupNames.add(setupName+"_"+split[split.length-3]);
		}else{
			return;
		}
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		MAPElitesPopulation pop = getMapPop(jbot,folder);
		System.out.println(folder);
		
		for(int x = 0 ; x < pop.getMap().length ; x++) {
			for(int y = 0 ; y < pop.getMap()[x].length ; y++) {
				
				MOChromosome res = pop.getMap()[x][y];
				
				if(res != null) {
					
					ExpandedFitness fit = (ExpandedFitness)res.getEvaluationResult();
					BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
					
					if(br instanceof VectorBehaviourExtraResult) {
						double[] behavior = (double[])br.value();
						Vector2d pos = new Vector2d(behavior[0],behavior[1]);
						
						double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
						double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
						fw.append(folderName+" "+setupName+" "+fitness+"\n");
					}
				}
			}
		}
	}
	
	private MAPElitesPopulation getMapPop(JBotEvolver jbot, String folder) throws Exception {
		
		String filename = folder+"/_showbest_current.conf";
		jbot.loadFile(filename, "--simulator +folder="+baseFolder);
		
		Simulator sim = jbot.createSimulator();
		jbot.createRobots(sim);
		jbot.setupBestIndividual(sim);
		MultipleWheelRepertoireActuator actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
		String string = baseFolder+actuator.getRepertoireLocation();
		
		File f = new File(string);
		jbot.loadFile(f.getParent()+"/_showbest_current.conf", "--simulator +folder="+baseFolder);
		
		return (MAPElitesPopulation)jbot.getPopulation();
	}
}