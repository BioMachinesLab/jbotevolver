package main;
import evolutionaryrobotics.ViewerMain;


public class CovMain {
	
	public static void main(String[] args) throws Exception {
		new ViewerMain(new String[]{"--gui","classname=ResultCoEvolutionViewerGui,renderer=(classname=TwoDRenderer))"});
	}
	
}
