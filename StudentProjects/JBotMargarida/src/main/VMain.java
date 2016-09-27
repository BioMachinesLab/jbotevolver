package main;

import gui.CombinedGui;

public class VMain {
	
	public VMain(String[] args) throws Exception{
		new CombinedGui(args);
	}
	
	public static void main(String[] args) {
		try {
			new CombinedGui(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRendererDebug,conesensorid=1))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}