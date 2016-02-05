package utils;

import java.io.File;
import java.util.Scanner;

public class CheckProgress {
	
//	static String prefix = "bigdisk/december2015/10samples/";static String m = "_obstacle/";static double maxGens = 500;
//	static String prefix = "bigdisk/december2015/foraging/";static String m = "_foraging/";static double maxGens = 300;
	
	static String prefix = "";static String m = "_obstacle/";static double maxGens = 500;
	
	static int maxRuns = 30;
	static int expected = 0;
	static int found = 0;
	
	public static void main(String[] args) throws Exception{
		String f = "";
		String[] setups = new String[]{/*f+"multiple_intersection_repertoire"+m,f+"single_intersection_repertoire"+m,*/f+"wheels"+m,f+"repertoire"+m,f+"all_repertoire"+m};
//		String[] setups = new String[]{f+"wheels"+m,f+"repertoire"+m,f+"all_repertoire"+m};
		
		for(String s : setups)
			new CheckProgress(prefix+s);

		System.out.println("Total: "+(found/(double)expected*100.0)+"% ("+(expected-found)/maxGens+" runs left)");
	}
	
	public CheckProgress(String folder) throws Exception{
		
		File f = new File(folder);
		
		String result = "";
		
		for(String s : f.list()) {
			result=checkSubFolders(new File(folder+s));
			System.out.println(result);
		}
		
	}
	
	private String checkSubFolders(File folder) throws Exception {
		
		String result = "";
		
		if(folder.list() == null) {
			return result;
		}
		
		if(new File(folder.getPath()+"/"+folder.getName()+".conf").exists()) {
			for(int i = 1 ; i <= maxRuns ; i++) {
				String fn = folder.getPath()+"/"+i;
				int gens = checkGeneration(fn);
				found+=gens;
				expected+=(maxGens-1);
				result+=fn+"\t"+gens+"\n";
			}
		} else {
			for(String f : folder.list()) {
				String fn = folder+"/"+f;
				if(new File(fn).isDirectory()) {
					result+= checkSubFolders(new File(fn));
				}
			}
		}
		
//		for(String f : folder.list()) {
//			
//			String fn = folder.getPath()+"/"+f;
//			
//			if(f.equals("_fitness.log")) {
//				result+=folder.getPath()+"\t"+checkGeneration(folder.getPath())+"\n";
//			} else if(new File(fn).isDirectory()) {
//				result+= checkSubFolders(new File(fn));
//			}
//		}
		return result;
	}
	
	private int checkGeneration(String folder) throws Exception {
		
		if(!new File(folder+"/_generationnumber").exists())
			return 0;
		
		return getHighestGeneration(folder);
	}
	
	
	public static int getHighestGeneration(String folder) throws Exception{
		File f = new File(folder);
		
		File post = new File(f.getPath()+"/_generationnumber");
		
		Scanner s = new Scanner(post);
		
		int gen = s.nextInt();
		
		s.close();
		
		return gen;
	}
}