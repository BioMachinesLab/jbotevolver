package evolutionaryrobotics;

public class ViewerMain {
	
	public ViewerMain(String[] args) throws Exception{
		new JBotEvolver(args);
	}
	
	public static void main(String[] args) {
		try {
			new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}