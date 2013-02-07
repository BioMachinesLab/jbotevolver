package gui;

import gui.renderer.Renderer;

import java.lang.reflect.Constructor;

import simulation.JBotSim;
import simulation.Updatable;
import simulation.util.Arguments;

/**
 * Base class for all GUIs. A GUI has control over a simulation run. A GUI need not display any windows. 
 * A {@link Renderer} is passed to the {@link run} method. The {@link Renderer} is responsible for displaying 
 * the virtual world. A GUI implementation can choose to ignore the {@link Renderer} provided and use a specific one (or even 
 * two or more {@link Renderer}s at the same time).
 * 
 * @author alc
 */

public abstract class Gui implements Updatable {
	
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

		String guiName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(guiName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == JBotSim.class && params[1] == Arguments.class) {
					return (Gui) constructor.newInstance(jBotSim, arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Gui with name '" + guiName + "' not found");
	}
}