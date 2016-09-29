package evorbc.executables;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import simulation.Executable;
import simulation.JBotSim;
import simulation.util.Arguments;
import simulation.util.Factory;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evorbc.mappingfunctions.MappingFunction;
import evorbc.mappingfunctions.Polar180MappingFunction;

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
		
		jbotsim.getSerializableObjectHashMap().put("repertoirepath", f);
		
		System.out.println("[LoadRepertoireExecutable] Loading repertoire from "+f);
		
		MOChromosome[][] repertoire = loadRepertoire(f);
		
//		MAPElitesEvolution.printRepertoire(repertoire);
		
		System.out.println("[LoadRepertoireExecutable] Loaded repertoire? "+(repertoire!= null));
		
		double[][][] map = new double[repertoire.length][repertoire[0].length][];
		
		for(int x = 0 ; x < map.length ; x++) {
			for(int y = 0 ; y < map[x].length ; y++) {
				if(repertoire[x][y] != null)
					map[x][y] = repertoire[x][y].getAlleles();
			}
		}
		
		boolean shrink = jbotsim.getArguments().get("--simulator").getArgumentAsIntOrSetDefault("shrink", 1) == 1;
		System.out.println("[LoadRepertoireExecutable] shrink "+shrink);
		if(shrink)
			map = shrink(map);
		
		if(args.getArgumentIsDefined("fill")) {
			Arguments fillArguments = new Arguments(args.getArgumentAsString("fill"));
			System.out.println("[LoadRepertoireExecutable] Filling with "+fillArguments.getArgumentAsString("classname"));
			MappingFunction mf = (MappingFunction)Factory.getInstance(fillArguments.getArgumentAsString("classname"),(Object)map);
			mf.fill();
		}

		jbotsim.getSerializableObjectHashMap().put("repertoire", map);
	}
	
	protected double[][][] shrink(double[][][] map) {
		
		int maxDist = 0;
		
		for(int x = 0 ; x < map.length ; x++) {
    		for(int y = 0 ; y < map[x].length ; y++) {
    			if(map[x][y] != null) {
    				int posY = y;
    				int posX = x; 
	    			posX-= map.length/2;
	        		posY-= map[0].length/2;
	        		maxDist = Math.max(maxDist,posX);
	        		maxDist = Math.max(maxDist,posY);
    			}
    		}
    	}
		
		
		double[][][] newMap = new double[maxDist*2+10][maxDist*2+10][];
		
		int half = map.length/2;
		int nhalf = newMap.length/2;
		
		for(int x = -maxDist ; x <= maxDist ; x++) {
    		for(int y = -maxDist ; y <= maxDist ; y++) {
    			
    			int newX = nhalf+x;
    			int newY = nhalf+y;
    			int oldX = half+x;
    			int oldY = half+y;
    			
    			newMap[newX][newY] = map[oldX][oldY];
    		}
		}
		
		if(map[map.length/2][map.length/2] != newMap[newMap.length/2][newMap.length/2]) {
			throw new RuntimeException("[LoadRepertoireExecutable] The shrinking of the map failed!");
		}
			
		return newMap;
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
