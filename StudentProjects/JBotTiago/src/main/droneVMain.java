package main;

import gui.CombinedGui;

public class droneVMain {

	public static void main(String[] args) {
		args = new String[]{"--gui","classname=CIResultViewerGui,renderer=(classname=CITwoDRenderer,seesensors=1,conesensorid=2)"};
		new CombinedGui(args);
	}
	
}
