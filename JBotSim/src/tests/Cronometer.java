package tests;

public class Cronometer {
	
	private static long startingTime;
	private static long lapTime;
	private static long[] times = new long[10];
	private static long[] timesStamp = new long[10];
	
	public static void start() {
		startingTime = System.currentTimeMillis();
		lapTime = startingTime;
	}
	
	public static void lap(String msg) {
		System.out.println(msg+": "+(System.currentTimeMillis()-lapTime));
		lapTime = System.currentTimeMillis();
	}
	
	public static void setupLap(int index) {
		timesStamp[index] = System.currentTimeMillis();
	}
	
	public static void lap(int index) {
		times[index]+= System.currentTimeMillis()-timesStamp[index];
	}
	
	public static void stop() {
		for(int i = 0 ; i < times.length ; i++)
			if(times[i] > 0)
				System.out.println(i+":"+times[i]);
		System.out.println("Total time: "+(System.currentTimeMillis()-startingTime));
		for(int i = 0 ; i < times.length ; i++)
			times[i] = 0;
		System.out.println();
	}

}
