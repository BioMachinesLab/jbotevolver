package vrep;

import taskexecutor.VREPResult;
import taskexecutor.VREPTask;
import taskexecutor.VREPTaskExecutor;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class VRepUtils {
	
	public static int sendTasks(VREPTaskExecutor te, Chromosome[] chromosomes, float[] fixedParameters, float type) {
		
		int instances = te.getInstances();
		int chromosomesPerInstance = chromosomes.length/instances;
		
		int remainder = chromosomes.length % instances;
		
		int totalIndex = 0;
		
		for(int i = 0 ; i < instances ; i++){
			
			int currentTasks = chromosomesPerInstance;
			
			if(remainder > 0) {
				currentTasks++;
				remainder--;
			}
			
			float[][] toSend = new float[currentTasks+1][];
			
			int index = 0;
			int toSendIndex = 0;
			
			float[] parameters = new float[fixedParameters.length + 1];
			
			for(int fp = 0 ; fp < fixedParameters.length ; fp++)
				parameters[index++] = fixedParameters[fp];

			parameters[index++] = currentTasks; //nInds
			toSend[toSendIndex++] = parameters;
			
			for(int ci = 0 ; ci < currentTasks ; ci++) {
				index = 0;
				Chromosome c = chromosomes[totalIndex++];
				
				float[] params = new float[3 + c.getAlleles().length];
				
				params[index++] = c.getID(); //id of the chromosome
				params[index++] = c.getAlleles().length + 1; //length of chromosome + type
				params[index++] = type; //type
				
				for(int j = 0 ; j < c.getAlleles().length ; j++) {
					params[index++] = (float)c.getAlleles()[j];
				}
				toSend[toSendIndex++] = params;
				
				System.out.print(".");
			}
			
			int count = 0;
			for(int c = 0 ; c < toSend.length ; c++)
				count+=toSend[c].length;
			
			float[] result = new float[count];
			int resultIndex = 0;
			for(int c = 0 ; c < toSend.length ; c++) {
				for(int d = 0 ; d < toSend[c].length ; d++) {
					result[resultIndex++] = toSend[c][d];
				}
			}
			
			VREPTask task = new VREPTask(result);
			te.addTask(task);
			System.out.print("\n");
		}
		
		return instances;
	}
	
	public static float[][] receiveTasks(VREPTaskExecutor te,int nTasks) {
		
		float[][] results = new float[nTasks][];
		
		for(int i = 0 ; i < nTasks ; i++){
			VREPResult res = (VREPResult) te.getResult();
			
			float[] vals = res.getValues();
			
			results[i] = vals;
			
			float nResults = vals[0];
			
			for(int n = 0 ; n < nResults ; n++)
				System.out.print("!");
			
			System.out.print("\n");
		}
		return results;
	}

}
