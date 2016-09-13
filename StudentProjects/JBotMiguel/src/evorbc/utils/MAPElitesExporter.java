package evorbc.utils;

import java.io.File;

import evolution.MAPElitesEvolution;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import gui.extended.MAPElitesTracer;
import simulation.Simulator;
import simulation.util.Arguments;
import utils.TraverseFolders;

public class MAPElitesExporter extends TraverseFolders{
	
	private static int MAX_RUN = 30;
	
	public static void main(String[] args) throws Exception {
		new MAPElitesExporter("bigdisk/time-binsize/").traverse();
//		new MAPElitesExporter("bigdisk2/").traverse();
//		bigdisk_binsize_repertoire_50_AWS_5Actuator_20_3
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		if(new File(folder.getPath()+"/repertoire_name.txt").exists()) {
		
			if(MAX_RUN > 0) {
				String[] split = folder.getPath().split("/");
				int run = Integer.parseInt(split[split.length-1]);
				if(run <= MAX_RUN)
					return true;
				else
					return false;
			}

			return true;
		}

		return false;
	}

	@Override
	protected void act(File folder) {
		
		System.out.println(folder);
		
		try {
			export(folder.getPath());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void export (String folder) throws Exception {
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf","--init","skip=1"});
		MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
		MAPElitesTracer t = new MAPElitesTracer(new Arguments("scale=500,folder=map_elites_img,name="+folder.replace("/", "_")));
		
		Simulator[] sims = new Simulator[1];
		sims[0] = jbot.createSimulator();
		
		MAPElitesPopulation[] pops = new MAPElitesPopulation[1];
		pops[0] = pop;
		
		String[] setups = new String[]{""};
		t.drawMapElites(sims[0], pop);
//		t.drawMapElites(setups,sims, pops);
	}
	
	public MAPElitesExporter(String folder) {
		super(folder);
	}
	
}