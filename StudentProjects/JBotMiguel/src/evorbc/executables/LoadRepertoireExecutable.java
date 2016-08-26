package evorbc.executables;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import multiobjective.MOChromosome;
import simulation.Executable;
import simulation.JBotSim;
import simulation.util.Arguments;
import simulation.util.Factory;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evorbc.mappingfunctions.MappingFunction;

public class LoadRepertoireExecutable implements Executable{
	
	public LoadRepertoireExecutable() {}
	
	@Override
	public void execute(JBotSim jbotsim, Arguments args) {
		
		String f = args.getArgumentAsString("repertoire");

		if(jbotsim.getArguments().get("--simulator") != null)  {
			Arguments simArgs = jbotsim.getArguments().get("--simulator");
			String prefix = simArgs.getArgumentAsStringOrSetDefault("folder","");
			f= prefix+f;
		}
		
		f+="/_showbest_current.conf";
		
		System.out.println("[LoadRepertoireExecutable] Loading repertoire from "+f);
		
		MOChromosome[][] repertoire = loadRepertoire(f);
		
		System.out.println("[LoadRepertoireExecutable] Loaded repertoire? "+(repertoire!= null));
		
		double[][][] map = new double[repertoire.length][repertoire[0].length][];
		
		for(int x = 0 ; x < map.length ; x++) {
			for(int y = 0 ; y < map[x].length ; y++) {
				if(repertoire[x][y] != null)
					map[x][y] = repertoire[x][y].getAlleles();
			}
		}
		
		if(args.getArgumentIsDefined("fill")) {
			Arguments fillArguments = new Arguments(args.getArgumentAsString("fill"));
			System.out.println("[LoadRepertoireExecutable] Filling with "+fillArguments.getArgumentAsString("classname"));
			MappingFunction mf = (MappingFunction)Factory.getInstance(fillArguments.getArgumentAsString("classname"),(Object)map);
			mf.fill();
		}
		
		jbotsim.getSerializableObjectHashMap().put("repertoire", map);
	}
	
	protected MOChromosome[][] loadRepertoire(String f) {
		
		try {
			JBotEvolver jbot = new JBotEvolver(new String[]{f});
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			return pop.getMap();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
}
