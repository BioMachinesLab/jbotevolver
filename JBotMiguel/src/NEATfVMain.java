import evolutionaryrobotics.JBotEvolver;

public class NEATfVMain {
	
	public static void main(String[] args) throws Exception {
		new JBotEvolver(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer,bigrobots=1))"});
	}
}
