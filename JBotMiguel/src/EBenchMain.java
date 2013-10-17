import evolutionaryrobotics.EvolverMain;

public class EBenchMain {
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		new EvolverMain(new String[]{"benchmark.conf"});
		System.out.println("TIME: "+(System.currentTimeMillis()-time));
	}
}
