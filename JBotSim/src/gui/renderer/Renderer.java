package gui.renderer;

import java.awt.Component;
import java.awt.Image;
import java.lang.reflect.Constructor;

import mathutils.Point2d;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 * Responsible for rendering the simulation. "Rendering" is broadly defined: a renderer may
 * display robots moving around in a virtual world, it can display statistics or it can save 
 * the simulation in a file.
 * 
 * @author alc
 *
 */
public abstract class Renderer extends Component implements Updatable {

	/**
	 * Draw one frame. Notice that this method may be called several times for the same simulation step 
	 * and it may not be called for all simulation steps.
	 */
	public abstract void drawFrame();

	/**
	 * Dispose of any resources allocated by the renderer.
	 */
	public abstract void dispose();

	/**
	 * Draw a circle in the virtual world.
	 *
	 * @param center center of the circle
	 * @param radius radius of the circle
	 */
	public abstract void drawCircle(Point2d center, double radius);
	
	public abstract void zoomIn();
	public abstract void zoomOut();
	public abstract void resetZoom();
	
	public static Renderer getRenderer(Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname")) {
			throw new RuntimeException("Renderer 'classname' not defined: "+ arguments.toString());
		}

		String robotName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(robotName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (Renderer) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown renderer: " + robotName);
	}
}