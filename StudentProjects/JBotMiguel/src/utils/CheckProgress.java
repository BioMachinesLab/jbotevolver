package utils;

import java.io.File;
import java.util.Scanner;

public class CheckProgress extends TraverseFolders {
	
	private double maxGens = 1000;
	private int maxRuns = 30;
	private int expected = 0;
	private int found = 0;
	
	public static void main(String[] args) throws Exception{
		new CheckProgress("bigdisk/evorbc2/",new String[]{});
	}
	
	public CheckProgress(String baseFolder, String[] setups, int generations, int runs) {
		super(baseFolder, setups);
		this.maxGens = generations;
		this.maxRuns = runs;
		traverse();
		System.out.println("Total: "+(found/(double)expected*100.0)+"% ("+(expected-found)/maxGens+" runs left)");
	}
	
	public CheckProgress(String baseFolder, String[] setups) {
		this(baseFolder, setups, 1000, 30);
	}
	
	protected void act(File folder) {
		
		try {
		
			String result = "";
			
			if(new File(folder.getPath()+"/"+folder.getName()+".conf").exists()) {
				for(int i = 1 ; i <= maxRuns ; i++) {
					String fn = folder.getPath()+"/"+i;
					int gens = checkGeneration(new File(fn));
					found+=gens;
					expected+=(maxGens-1);
					result+=fn+"\t"+gens+"\n";
				}
			} 
			
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