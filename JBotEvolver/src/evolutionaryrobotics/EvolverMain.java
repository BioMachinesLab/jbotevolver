package evolutionaryrobotics;

public class EvolverMain {
	
	public static void main(String[] args) throws Exception {
		JBotEvolver j = new JBotEvolver(args);
		j.getEvolution().executeEvolution();
	}
}