package main;

import gui.CombinedGui;

public class InesVMain {

	public InesVMain(String[] args) throws Exception{
		new CombinedGui(args);
	}
	
	public static void main(String[] args) {
		try {
			new CombinedGui(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer,drawarea=1))"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}