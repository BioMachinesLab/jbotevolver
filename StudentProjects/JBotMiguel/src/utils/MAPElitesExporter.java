package utils;

import evolution.MAPElitesEvolution;
import evolution.MAPElitesPopulation;
import gui.extended.MAPElitesTracer;
import simulation.Simulator;
import simulation.util.Arguments;

public class MAPElitesExporter extends MAPElitesViewer{
	
	
	public static void main(String[] args) throws Exception {
//		new MAPElitesViewer("../../EvolutionAutomator/");
		new MAPElitesExporter("repertoire/");
//		new MAPElitesViewer("intersected_repertoires");
		
	}
	
	public MAPElitesExporter(String folder) {
		super(folder);
//		export();
		exportAll();
	}
	
	private void exportAll() {
		
		MAPElitesPopulation[] pops = new MAPElitesPopulation[files.size()];
		Simulator[] sims = new Simulator[files.size()];
		
		int i = 0;
		
		String[] files = new String[]{"AWS_8Actuator_30/1","AWS_3Actuator_30/1","AWS_5Actuator_30/1","FWS_2Actuator_30/1","FWS_3Actuator_30/1"};
//		String[] files = new String[]{"FWS_2Actuator_30/1","FWS_3Actuator_30/1","AWS_3Actuator_30/1","AWS_5Actuator_30/1","AWS_8Actuator_30/1"};
		
		for(String f : files) {
			
			System.out.println(f);
			loadFile(f);
			
				setGeneration(currentGens-1);	
			
			f = f.replaceAll("/", "_");
			
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
//			MAPElitesEvolution.expandToCircle(pop,0.8);
			sims[i] = sim;
			pops[i] = pop;
			i++;
			
		}
		
		MAPElitesTracer t = new MAPElitesTracer(new Arguments("scale=500,folder=map_elites_img,name=repertoire_all"));
		t.drawMapElites(new String[]{"4WS-8","4WS-3","4WS-5","2WS-2","2WS-3",},sims, pops);
	}
	
	private void export() {
		
		String[] imgNames = new String[]{"1_original","2_pruned","3_filled"};
		
		for(String n : imgNames) {
			for(String f : files) {
				
				System.out.println(f);
				loadFile(f);
				
				if(n.equals("3_filled"))
					setGeneration(currentGens);
				else
					setGeneration(currentGens-1);	
				
				f = f.replaceAll("/", "_");
				
				MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
//				MAPElitesEvolution.expandToCircle(pop);
				if(n.equals("2_pruned"))
					MAPElitesEvolution.prune(pop, 0.8);
				
				MAPElitesTracer t = new MAPElitesTracer(new Arguments("scale=500,folder=map_elites_img,name="+f+"_"+n));
				t.drawMapElites(sim, pop);
				
			}
		}
	}
}