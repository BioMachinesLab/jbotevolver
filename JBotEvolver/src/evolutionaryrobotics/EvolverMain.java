package evolutionaryrobotics;

public class EvolverMain {
	
	public static void main(String[] args) throws Exception {
		args = new String[]{"left_primitive.conf"};
		JBotEvolver j = new JBotEvolver(args);
		j.getEvolution().executeEvolution();
		j.evolutionFinished();
	}
}