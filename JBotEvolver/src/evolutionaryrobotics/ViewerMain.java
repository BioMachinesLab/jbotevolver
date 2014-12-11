package evolutionaryrobotics;

import gui.CombinedGui;

public class ViewerMain {
	
	public ViewerMain(String[] args) throws Exception{
		new JBotEvolver(args);
	}
	
	public static void main(String[] args) {
		try {
			new CombinedGui(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}