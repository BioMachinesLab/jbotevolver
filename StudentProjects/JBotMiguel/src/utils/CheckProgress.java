package utils;

import java.io.File;
import java.util.Scanner;

public class CheckProgress extends TraverseFolders {
	
	private static boolean VERBOSE = false;
	
	private int maxGens = 1000;
	private int maxRuns = 30;
	private int expected = 0;
	private int found = 0;
	
	public static void main(String[] args) throws Exception{
		
		int g = 1000;
		int r = 30;
		
		new CheckProgress("ann/",new String[]{},r,g).traverse();//done
		new CheckProgress("behaviormapping/",new String[]{},r,g).traverse();//done
		new CheckProgress("multimaze/",new String[]{},r,g).traverse();
		new CheckProgress("qualitymetrics/",new String[]{},r,g).traverse();
		new CheckProgress("repertoireresolution/",new String[]{},r,g).traverse();
		new CheckProgress("repertoiresize/",new String[]{},r,g).traverse();
	}
	
	public CheckProgress(String baseFolder, String[] setups, int runs, int generations) {
		super(baseFolder, setups);
		this.maxGens = generations;
		this.maxRuns = runs;
	}
	
	@Override
	public void traverseEnded() {
		System.out.println("Total: "+(found/(double)expected*100.0)+"% ("+(expected-found)/maxGens+" runs left) -- "+baseFolder);
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		if(new File(folder.getPath()+"/repertoire_name.txt").exists())
			return false;
		
		if(new File(folder.getPath()+"/"+folder.getName()+".conf").exists()) {
			if(new File(folder.getPath()+"/1/repertoire_name.txt").exists())
				return false;
			
			return true;
		}
		
		return false;
	}
	
	protected void act(File folder) {
		
		try {
		
			String result = "";
			
			for(int i = 1 ; i <= maxRuns ; i++) {
				String fn = folder.getPath()+"/"+i;
				int gens = checkGeneration(new File(fn));
				found+=gens;
				expected+=(maxGens-1);
				result+=fn+"\t"+gens+"\n";
			}
			
			if(VERBOSE)
				System.out.println(result);
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private int checkGeneration(File folder) throws Exception {
		
		if(!new File(folder.getPath()+"/_generationnumber").exists())
			return 0;
		
		return getHighestGeneration(folder);
	}
	
	
	public static int getHighestGeneration(File folder) throws Exception{
		File post = new File(folder.getPath()+"/_generationnumber");
		Scanner s = new Scanner(post);
		int gen = s.nextInt();
		s.close();
		return gen;
	}
}