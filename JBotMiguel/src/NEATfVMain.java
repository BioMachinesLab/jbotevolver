import extensions.ExtendedJBotEvolver;

public class NEATfVMain {
	
	public static void main(String[] args) throws Exception {
		new ExtendedJBotEvolver(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer))"});
	}
}
