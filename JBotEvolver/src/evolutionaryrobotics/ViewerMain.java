package evolutionaryrobotics;

public class ViewerMain {
	public static void main(String[] args) throws Exception {
		new JBotEvolver(new String[]{"--gui","classname=ResultCoEvolutionViewerGui,renderer=(classname=TwoDRenderer))"});
	}
}