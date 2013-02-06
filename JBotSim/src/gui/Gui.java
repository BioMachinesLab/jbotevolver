package gui;

import java.lang.reflect.Constructor;

import simulation.Simulator;
import simulation.Updatable;
import simulation.util.Arguments;
import gui.renderer.BlenderRenderer;
import gui.renderer.NullRenderer;
import gui.renderer.Renderer;
import gui.renderer.TraceRenderer;
import gui.renderer.TwoDRenderer;
import gui.renderer.TwoDRendererDebug;

/**
 * Base class for all GUIs. A GUI has control over a simulation run. A GUI need not display any windows. 
 * A {@link Renderer} is passed to the {@link run} method. The {@link Renderer} is responsible for displaying 
 * the virtual world. A GUI implementation can choose to ignore the {@link Renderer} provided and use a specific one (or even 
 * two or more {@link Renderer}s at the same time).
 * 
 * @author alc
 */

public abstract class Gui implements Updatable {
	
	public Gui(Arguments args) {}

	/**
	 * Dispose of any windows, dialogs and other resources allocated by the GUI.
	 */
	public abstract void dispose();
	
	public static Gui getGui(Arguments arguments) throws Exception {
		
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Gui 'classname' not defined: "+ arguments.toString());

		String guiName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(guiName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (Gui) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Gui with name '" + guiName + "' not found");
	}
}