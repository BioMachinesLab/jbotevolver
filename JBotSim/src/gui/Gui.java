package gui;

import javax.swing.JPanel;
import simulation.JBotSim;
import simulation.util.Arguments;
import simulation.util.Factory;

public abstract class Gui extends JPanel {
	
	protected JBotSim jBotSim;
	
	public Gui(JBotSim jBotSim, Arguments args) {
		this.jBotSim = jBotSim;
	}

	/**
	 * Dispose of any windows, dialogs and other resources allocated by the GUI.
	 */
	public abstract void dispose();
	
	public static Gui getGui(JBotSim jBotSim, Arguments arguments) {
		
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Gui 'classname' not defined: "+ arguments.toString());
		
		return (Gui)Factory.getInstance(arguments.getArgumentAsString("classname"),jBotSim,arguments);
	}
	
}