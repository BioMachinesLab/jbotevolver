package evorbc.utils;

import evolution.MAPElitesEvolution;
import evolution.MAPElitesPopulation;
import gui.extended.MAPElitesTracer;
import simulation.Simulator;
import simulation.util.Arguments;

public class MAPElitesExporter extends MAPElitesViewer{
	
	protected String imageName;
	
	public static void main(String[] args) throws Exception {
		new MAPElitesExporter("bigdisk/qualitymetrics/repertoire_distance/","distance_old");
		new MAPElitesExporter("bigdisk/qualitymetrics/repertoire_radial/","radial_old");
		new MAPElitesExporter("bigdisk/qualitymetrics/repertoire/","circular_old");
		new MAPElitesExporter("qualitymetric_test/repertoire_distance/","distance_new");
		new MAPElitesExporter("qualitymetric_test/repertoire_circular/","circular_new");
		new MAPElitesExporter("qualitymetric_test/repertoire_radial/","radial_new");
		new MAPElitesExporter("qualitymetric_test/repertoire_radial_dist/","radial_dist_new");
		new MAPElitesExporter("qualitymetric_test/repertoire_circular_dist/","circular_dist_new");
	}
	
	public MAPElitesExporter(String folder, String imageName) {
		super(folder);
		this.imageName = imageName;
		exportAll();
	}
	
	private void exportAll() {
		
		MAPElitesPopulation[] pops = new MAPElitesPopulation[files.size()];
		String[] setups = new String[]{"4WS-5","2WS-3"};
		Simulator[] sims = new Simulator[setups.length];
		
		int i = 0;
		
		String[] files = new String[]{"AWS_5Actuator_20/1","FWS_3Actuator_20/1"};
		
		for(String f : files) {
			
			System.out.println(f);
			loadFile(f);
			
				setGeneration(currentGens-1);	
			
			f = f.replaceAll("/", "_");
			
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			sims[i] = sim;
			pops[i] = pop;
			i++;
			
		}
		
		MAPElitesTracer t = new MAPElitesTracer(new Arguments("scale=500,folder=map_elites_img,name="+imageName));
		t.drawMapElites(setups,sims, pops);
	}
	
	/*
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
	}*/
}