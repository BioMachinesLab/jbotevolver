package evolution;

import java.io.File;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.util.DiskStorage;

public class MAPElitesIntersection {
	
	
	public static void main(String[] args) throws Exception{
		
		String[] repertoires_fws_1 = new String[]{"repertoires/fws2/1","repertoires/fws3/1","repertoires/fws4/1"};
		String[] repertoires_fws_2 = new String[]{"repertoires/fws2_2/1","repertoires/fws3_2/1","repertoires/fws4_2/1"};
		
		String[] repertoires_aws_1 = new String[]{"repertoires/aws3/1","repertoires/aws4/1","repertoires/aws5/1","repertoires/aws6/1","repertoires/aws8/1"};
		String[] repertoires_aws_2 = new String[]{"repertoires/aws3_2/1","repertoires/aws4_2/1","repertoires/aws5_2/1","repertoires/aws6_2/1","repertoires/aws8_2/1"};
		
		intersect(repertoires_fws_1);
		intersect(repertoires_fws_2);
		
		intersect(repertoires_aws_1);
		intersect(repertoires_aws_2);
	}
	
	public static void intersect(String[] repertoires) throws Exception{
		intersect("",repertoires);
	}
	
	public static void intersect(String suffix, String[] repertoires) throws Exception{
		
		double pruneThreshold = 0.8;
		boolean printRepertoire = false;
		
		boolean[][] result = null;
		
		System.out.println("################## INPUTS");
		for(String s : repertoires) {
			System.out.println("\n######### "+s);

			String file = s+"/_showbest_current.conf";
			JBotEvolver jbot = new JBotEvolver(new String[]{file});
			
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			MOChromosome[][] map = pop.getMap();
			
			if(result == null) {
				result = new boolean[map.length][map[0].length];
				//initialize the mask with the first map
				for(int x = 0 ; x < map.length ; x++) {
					for(int y = 0 ; y < map[0].length ; y++) {

						int[] loc = getSupposedPosition(x, y, pop);
						
						if(loc != null) {
							result[x][y] = true;
						} else {
							map[x][y] = null;
						}
						
					}
				}
			} else {
				//remove all locations that are not shared by all maps
				for(int x = 0 ; x < map.length ; x++) {
					for(int y = 0 ; y < map[x].length ; y++) {
						
						int[] loc = getSupposedPosition(x, y, pop);
						
						if(loc == null) {
							result[x][y] = false;
							map[x][y] = null;
						}
					}
				}
			}
			
			if(printRepertoire)
				MAPElitesEvolution.printRepertoire(pop);
		}
		
		//create a resulting map for each repertoire that is the intersection of the mask with the chromosomes in the repertoire
		System.out.println("\n################## RESULTS");
		
		String folderName = "intersected_repertoire";
		
		if(!suffix.isEmpty()) {
			folderName+="_"+suffix;
		}
		
		File folder = new File(folderName);
		
		if(!folder.exists())
			folder.mkdir();
		
		for(String s : repertoires) {
			System.out.println("\n######### "+s);
			
			File currentFolder = new File(folderName+"/"+s);
			currentFolder.mkdirs();
			
			String file = s+"/_showbest_current.conf";
			JBotEvolver jbot = new JBotEvolver(new String[]{file});
			
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			MOChromosome[][] map = pop.getMap();
			
			for(int x = 0 ; x < map.length ; x++) {
				for(int y = 0 ; y < map[0].length ; y++) {
					if(!result[x][y]) {
						map[x][y] = null;
					}
				}
			}
			
			if(printRepertoire)
				MAPElitesEvolution.printRepertoire(pop);
			
			MAPElitesEvolution.prune(pop,pruneThreshold);
			MAPElitesEvolution.expandToCircle(pop);
			
			if(printRepertoire)
				MAPElitesEvolution.printRepertoire(pop);
			
			MAPElitesEvolution.saveRepertoireTxt(folderName+"/",s.replace('/', '_')+".txt",pop);
			MAPElitesEvolution.saveRepertoireTxt(folderName+"/"+s+"/","repertoire.txt",pop);
			
			DiskStorage diskStorage = new DiskStorage(folderName+"/"+s);
			diskStorage.saveCommandlineArguments(jbot.getArguments());
			diskStorage.start();
			diskStorage.savePopulation(pop);
			diskStorage.close();
			
		}
	}
	
	public static int[] getSupposedPosition(int x, int y, MAPElitesPopulation pop) {
		
		MOChromosome res = pop.getMap()[x][y];
		
		if(res != null) {
			ExpandedFitness fit = (ExpandedFitness)res.getEvaluationResult();
			BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
			
			if(br instanceof VectorBehaviourExtraResult) {
				double[] behavior = (double[])br.value();
				Vector2d pos = new Vector2d(behavior[0],behavior[1]);
				
				int[] supposedLocation = pop.getLocationFromBehaviorVector(behavior);
				
				pos.x = (x-pop.getMap().length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
				pos.y = (y-pop.getMap()[x].length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
				
				if(supposedLocation[0] != x || supposedLocation[1] != y)
					return null;
				
				return supposedLocation; 
			}
		}
		return null;
	}
}
