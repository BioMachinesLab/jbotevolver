import evolutionaryrobotics.EvolverMain;

public class EBenchMain2 {
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		new EvolverMain(new String[]{"benchmark2.conf"});
		System.out.println("TIME2: "+(System.currentTimeMillis()-time));
	}
}
