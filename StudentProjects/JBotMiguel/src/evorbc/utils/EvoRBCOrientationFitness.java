package evorbc.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import utils.TraverseFolders;
import evaluationfunctions.DistanceTravelledEvaluationFunction;
import evaluationfunctions.OrientationEvaluationFunction;
import evaluationfunctions.RadialOrientationEvaluationFunction;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import fourwheeledrobot.MultipleWheelRepertoireActuator;

public class EvoRBCOrientationFitness extends TraverseFolders{
	
	private static String FOLDER_NAME = "export";
	
	private String fileName;
	private FileWriter fw;
	private boolean distance = false;
	private LinkedList<String> setupNames = new LinkedList<String>();
	
	public EvoRBCOrientationFitness(String baseFolder, String[] setups, String fileName) throws IOException {
		super(baseFolder, setups);
		this.fileName = fileName;
	}
	
	public static void main(String[] args) throws Exception{
		
		EvoRBCOrientationFitness orientationError = null;
//		orientationError = new EvoRBCOrientationFitness("bigdisk/repertoireresolution/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_50/","maze_quality_100/","maze_quality_200/"}, "repertoireresolution.txt");
		orientationError = new EvoRBCOrientationFitness("bigdisk/qualitymetrics/", new String[]{"maze_radial/","maze_distance/","maze_quality/"}, "distance-qualitymetrics.txt");
		orientationError.distance = true;
		orientationError.traverse(); System.out.println("Done "+orientationError.fileName);
	}
	
	@Override
	protected void traverseStarted() {
		
		try {
		
			new File(FOLDER_NAME).mkdir();
			new File(FOLDER_NAME+"/orientation-fitness-"+this.fileName).delete();
			fw = new FileWriter(new File(FOLDER_NAME+"/orientation-fitness-"+this.fileName));
			
			if(distance)
				fw.append("Folder Setup Distance Orientation\n");
			else
				fw.append("Folder Setup Orientation\n");
		
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
		String setupName = split[split.length-2]+"_"+split[split.length-3];
		
		if(!setupNames.contains(setupName)) {
			setupNames.add(setupName);
		}else{
			return;
		}
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		String popFolder = getMapPop(jbot, folder);
		System.out.println(popFolder);
		jbot = new JBotEvolver(new String[]{popFolder});
		MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
		
		for(int x = 0 ; x < pop.getMap().length ; x++) {
			for(int y = 0 ; y < pop.getMap()[x].length ; y++) {
				
				MOChromosome res = pop.getMap()[x][y];
				
				if(res == null)
					continue;
				
				Simulator sim = jbot.createSimulator();
				sim.addRobots(jbot.createRobots(sim, res));
				sim.addCallback(jbot.getEvaluationFunction()[0]);
				sim.setupEnvironment();
				
				sim.simulate();
				
				EvaluationFunction ff = (EvaluationFunction)sim.getCallbacks().get(0);
				
				if(ff instanceof DistanceTravelledEvaluationFunction) {
					
					DistanceTravelledEvaluationFunction ff1 = (DistanceTravelledEvaluationFunction)ff;
					fw.append(folderName+" "+setupName+" "+ff1.getFitness()+" "+0+"\n");
					
				} else if (ff instanceof RadialOrientationEvaluationFunction) {
					
					RadialOrientationEvaluationFunction ff1 = (RadialOrientationEvaluationFunction)ff;
					fw.append(folderName+" "+setupName+" "+ff1.getDistanceFitness()+" "+ff1.getOrientationFitness()+"\n");
					
				} else if (ff instanceof OrientationEvaluationFunction) {
					
					OrientationEvaluationFunction ff1 = (OrientationEvaluationFunction)ff;
					fw.append(folderName+" "+setupName+" "+ff1.getDistanceFitness()+" "+ff1.getOrientationFitness()+"\n");
					
				} else {
					throw new RuntimeException("Found an unexpected Evaluation Function!");
				}
				
//				if(res != null) {
//					
//					ExpandedFitness fit = (ExpandedFitness)res.getEvaluationResult();
//					BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
//					double distanceFitness = distEval.getFitness();
//					
//					if(br instanceof VectorBehaviourExtraResult) {
//						double[] behavior = (double[])br.value();
//						Vector2d pos = new Vector2d(behavior[0],behavior[1]);
//						double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
//						double orientationFitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
//						fw.append(folderName+" "+setupName+" "+distanceFitness+" "+orientationFitness+"\n");
//					}
//				}
			}
		}
	}
	
	private String getMapPop(JBotEvolver jbot, String folder) throws Exception {
		
		String filename = folder+"/_showbest_current.conf";
		jbot.loadFile(filename, "--simulator +folder="+baseFolder);
		
		Simulator sim = jbot.createSimulator();
		jbot.createRobots(sim);
		jbot.setupBestIndividual(sim);
		MultipleWheelRepertoireActuator actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
		String string = baseFolder+actuator.getRepertoireLocation();
		
		return new File(string).getParent().concat("/_showbest_current.conf");
		
//		File f = new File(string);
//		jbot.loadFile(f.getParent()+"/_showbest_current.conf", "--simulator +folder="+baseFolder);
//		
//		return (MAPElitesPopulation)jbot.getPopulation();
	}
}