package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
		
//		new CheckProgress("bigdisk2/vsvanilla/",new String[]{},r,g).traverse();
//		new CheckProgress("bigdisk2/behaviormapping/",new String[]{},r,g).traverse();
//		new CheckProgress("bigdisk2/qualitymetrics/",new String[]{},r,g).traverse();
//		new CheckProgress("bigdisk/binsize/",new String[]{},r,g).traverse();
		new CheckProgress("bigdisk/time-binsize/",new String[]{},r,g).traverse();
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
		BufferedReader bf = new BufferedReader(new FileReader(post));
		int gen = Integer.parseInt(bf.readLine());
		bf.close();
		return gen;
	}
}