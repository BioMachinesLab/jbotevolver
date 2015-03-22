package main;

import evolutionaryrobotics.ViewerMain;
import gui.CombinedGui;

public class cVMain {

	public static void main(String[] args) {
		try {
			new ViewerMain(new String[]{"--gui","classname=TiagoResultViewerGui,enabledebugoptions=1,renderer=(classname=TwoDRendererDebug,conesensorid=2)"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
