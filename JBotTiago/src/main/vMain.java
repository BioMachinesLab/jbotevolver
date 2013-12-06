package main;

import evolutionaryrobotics.ViewerMain;

public class vMain {

	public static void main(String[] args) throws Exception {
//		new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRendererDebug,conesensorid=1,coneclass=SimplePreySensor))"});
		new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRendererDebug,conesensorid=1,coneclass=WallRaySensor))"});
//		new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer,conesensorid=1))"});
	}
	
}
