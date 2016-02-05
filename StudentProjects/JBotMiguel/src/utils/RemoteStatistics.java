package utils;

public class RemoteStatistics {
	
	public static void main(String[] args) throws Exception{
		if(args[0].equals("1")) {
			CheckFitness.main(args);
		} else if(args[0].equals("2")) {
			ExportFitnessPlots.main(args);
		} else if(args[0].equals("3")) {
			IntersectionTest.main(args);
		} else if(args[0].equals("4")) {
			CheckProgress.main(args);
		}
	}

}
