package evorbc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;
import multiobjective.MOChromosome;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;

public class RevertRepertoireSelection {
	
	public static void main(String[] args) throws Exception{
		
		String[] files = new String[]{"hexamaze_1","hexamaze_2","hexamaze_3","hexamaze_4","hexamaze_5"};
		String folder = "hexapod_map/";
		
//		files = new String[]{"nao_1","nao_2","nao_3","nao_4","nao_5"};
//		folder = "nao_map_stop/";
		
		for(String file : files) {
		
			String fill=",fill=(classname=Polar180MappingFunction)";
	//		fill="";
	
			JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf","--init","LoadRepertoireExecutable=(repertoire=("+folder+")"+fill+",classname=LoadRepertoireExecutable)","--simulator","+shrink=0"});
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			
			Scanner s = new Scanner(new File("vrep_export/"+file+".txt"));
			
			double[][][] doubleMap = (double[][][])jbot.getSerializableObjectHashMap().get("repertoire");
			MOChromosome[][] map = pop.getMap();
			
			StringBuffer sb = new StringBuffer();
			
			while(s.hasNextLine()) {
				String l = s.nextLine();
				String[] elements = l.split(" ");
				
				int binX = (int)Double.parseDouble(elements[elements.length-2]);
				int binY = (int)Double.parseDouble(elements[elements.length-1]);
				
	//			MappingFunction.printRepertoire(doubleMap, binX, binY);
				
				int[] loc = null;
				
				double[] alleles = doubleMap[binX][binY];
				
				for(int x = 0 ; x < map.length ; x++) {
					for(int y = 0 ; y < map[x].length ; y++) {
						if(map[x][y] == null)
							continue;
						MOChromosome c = map[x][y];
						if(Arrays.equals(c.getAlleles(), alleles)){
							loc = new int[]{x,y};
							break;
						}
					}
					if(loc != null)
						break;
				}
				
				sb.append(l);
				sb.append(" ");
				sb.append(loc[0]);
				sb.append(" ");
				sb.append(loc[1]);
				sb.append("\n");
			}
			
			s.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("vrep_export/"+file+"-fixed.txt")));
			bw.write(sb.toString());
			bw.close();
		}
	}

}
