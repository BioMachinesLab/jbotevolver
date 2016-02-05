package main;

import gui.CombinedGui;

public class InesVMain {

	public InesVMain(String[] args) throws Exception{
		new CombinedGui(args);
	}
	
	public static void main(String[] args) {
		try {
			new CombinedGui(new String[]{"--gui","classname=ResultCoEvolutionViewerGui,renderer=(classname=InesTwoDRenderer,conesensorid=4,drawarea=1))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}