package main;

import javax.swing.JFrame;

import evolutionaryrobotics.JBotEvolver;
import gui.Gui;

public class vMain {

	public static void main(String[] args) throws Exception {
//		new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRendererDebug,coneclass=WallRaySensor))"}
//		new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer,conesensorid=1))"}
		
		try {
			args = new String[]{"--gui","classname=TiagoResultViewerGui,enabledebugoptions=1,renderer=(classname=TwoDRendererDebug,conesensorid=2)"};
			JBotEvolver jBotEvolver = new JBotEvolver(args);
			JFrame frame = new JFrame();
			frame.add(Gui.getGui(jBotEvolver, jBotEvolver.getArguments().get("--gui")));
			frame.setSize(1200, 860);
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
