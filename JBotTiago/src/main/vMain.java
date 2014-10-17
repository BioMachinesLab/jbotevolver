package main;

import evolutionaryrobotics.ViewerMain;

public class vMain {

	public static void main(String[] args) throws Exception {
		new ViewerMain(new String[]{"--gui","classname=TiagoResultViewerGui,renderer=(classname=TwoDRendererDebug,conesensorid=2)"});
//		new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRendererDebug,coneclass=WallRaySensor))"});
//		new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer,conesensorid=1))"});
	}
	
}
