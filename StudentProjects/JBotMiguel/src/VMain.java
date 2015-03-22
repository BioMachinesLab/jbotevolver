import evolutionaryrobotics.ViewerMain;
import gui.CombinedGui;

public class VMain {
	
	public static void main(String[] args) throws Exception {
		new CombinedGui(new String[]{"--gui","classname=ResultViewerExtended,enabledebugoptions=1,renderer=(classname=TwoDRenderer))"});
	}
}
