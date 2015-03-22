import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import neat.evolution.ERNEATPopulation;
import neat.layerered.ANNNeuron;
import neat.layerered.LayeredANN;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATPopulation;
import evolutionaryrobotics.JBotEvolver;

public class NEATComplexityMeasures {
	
	public static void main(String[] args) {
		
		String[] folders = {"bigdisk/october2014/tmaze/arbitrator_continuous/"};
		
		int everyGeneration = 10;
		
		for(String s : folders) {
			
			File f = new File(s);
			
			if(!f.exists())
				throw new RuntimeException("Folder "+s+" not found!");
			
			File[] subDirectories = f.listFiles();
			
			String[][] strings = new String[100][1000];
			
			int nRun = 0;
			
			for(int d = 1 ; d < strings.length ; d++) {
				
				File dir = new File(s+d);
				
				if(dir.isDirectory()) {
					System.out.println("# "+dir.getAbsolutePath());
					int generationNumber = getGenerationNumber(dir);
					
					try {
						
						int nGen = 0;
						
						for(int i = 0 ; i < generationNumber ; i+=everyGeneration) {
							File popFile = new File(dir.getCanonicalPath()+"/populations/population"+i);
							
							if(popFile.exists()) {
								JBotEvolver jbot = new JBotEvolver(new String[]{s+"/"+dir.getName()+"/show_best/showbest"+i+".conf"});
								ERNEATPopulation p = (ERNEATPopulation)jbot.getPopulation();
								strings[nRun][nGen++] = p.getBestGenome().getScore()+" "+getConnectionsPerNeuron(p)+" "+p.getBestChromosome().getAlleles().length;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					nRun++;
				} else
					break;
			}
			for(int gen = 0 ; gen < strings[0].length ; gen++) {
				System.out.print(gen*everyGeneration+" ");
				for(int run = 0 ; run < strings.length ; run++) {
					if(strings[run][gen] != null)
						System.out.print(strings[run][gen]+" ");
				}
				System.out.println();
			}
			
		}
	}
	
	public static String getConnectionsPerNeuron(ERNEATPopulation pop) {
		NEATPopulation p = pop.getPopulation();
		MLMethod net = p.getCODEC().decode(p.getBestGenome());
		ArrayList<ANNNeuron> neurons;
		
		if(net instanceof LayeredANN) {
			neurons = ((LayeredANN)net).getAllNeurons();
		} else {
			throw new RuntimeException("Not implemented for any other network type yet");
		}
		
		double max = 0;
		double min = 0;
		double total = 0;
		double count = 0;
		
		for(ANNNeuron n : neurons) {
			if(n.getType() != ANNNeuron.INPUT_NEURON && n.getType() != ANNNeuron.BIAS_NEURON) {
				int val = n.getIncomingConnections().size();
				max = Math.max(val, max);
				min = Math.min(val, min);
				total+=val;
				count++;
			}
		}
		
		double avg = total/count;
		
		return min+" "+avg+" "+max+" "+count+" "+total;
	}
	
	public static int getGenerationNumber(File directory) {
		
		File generationFile = new File(directory.getAbsolutePath()+"/_generationnumber");
		
		try {
			return new Scanner(generationFile).nextInt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
}