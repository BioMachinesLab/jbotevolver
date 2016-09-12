package main;

import evolutionaryrobotics.ViewerMain;
import gui.CombinedGui;

public class ritaMain {
	public static void main (String [] args){
		try {
			new CombinedGui(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=RitaTwoDRenderer))"});
			//new CombinedGui(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRendererDebug))"});
			//new CombinedGui(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=MyTraceRender))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
