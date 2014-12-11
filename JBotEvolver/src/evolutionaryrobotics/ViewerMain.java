package evolutionaryrobotics;

import gui.CombinedGui;

public class ViewerMain {
	
	public ViewerMain(String[] args) throws Exception{
		new CombinedGui(args);
	}
	
	public static void main(String[] args) {
		try {
			new CombinedGui(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}