package utils;

import evolution.MAPElitesEvolution;
import evolution.MAPElitesPopulation;
import gui.extended.MAPElitesTracer;
import simulation.util.Arguments;

public class MAPElitesExporter extends MAPElitesViewer{
	
	
	public static void main(String[] args) throws Exception {
//		new MAPElitesViewer("../../EvolutionAutomator/");
		new MAPElitesExporter("repertoire/");
//		new MAPElitesViewer("intersected_repertoires");
		
	}
	
	public MAPElitesExporter(String folder) {
		super(folder);
		export();
	}
	
	private void export() {
		for(String f : files) {
			
			if(f.contains("_20"))
				continue;

			System.out.println(f);
			loadFile(f);
			
			setGeneration(currentGens-1);
			
			f = f.replaceAll("/", "_");
			
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			MAPElitesEvolution.prune(pop, 0.8);
			
			MAPElitesTracer t = new MAPElitesTracer(new Arguments("scale=500,folder=map_elites_img,name="+f));
			t.drawMapElites(sim, pop);
			
		}
	}
}