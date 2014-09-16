import evolutionaryrobotics.ViewerMain;

public class VMain {
	
	public static void main(String[] args) throws Exception {
		new ViewerMain(new String[]{"--gui","classname=ResultViewerExtended,enabledebugoptions=1,renderer=(classname=TwoDRenderer))"});
	}
}
