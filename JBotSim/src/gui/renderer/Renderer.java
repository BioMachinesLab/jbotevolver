package gui.renderer;

import java.awt.Component;
import java.awt.Image;

import mathutils.Point2d;

import simulation.Simulator;

/**
 * Responsible for rendering the simulation. "Rendering" is broadly defined: a renderer may
 * display robots moving around in a virtual world, it can display statistics or it can save 
 * the simulation in a file.
 * 
 * @author alc
 *
 */
public interface Renderer {
	/**
	 * Get the {@link Component} for this Renderer (if any).
	 * @return a component that can be added to a AWT or Swing component.
	 */
	public Component getComponent();
	
	/**
	 * Set the simulator to renderer.
	 * 
	 * @param simulator simulator to render.
	 */
	public void setSimulator(Simulator simulator);

	/**
	 * Draw one frame. Notice that this method may be called several times for the same simulation step 
	 * and it may not be called for all simulation steps.
	 */
	public void drawFrame();

	/**
	 * Dispose of any resources allocated by the renderer.
	 */
	public void dispose();

	/**
	 * Get the number of the selected robot if any.
	 * @return number of the selected robot or -1 if no robot is currently selected.
	 */
	public int getSelectedRobot();

	/**
	 * Draw a circle in the virtual world.
	 *
	 * @param center center of the circle
	 * @param radius radius of the circle
	 */
	public void drawCircle(Point2d center, double radius);
	
	/**
	 * Draws an image in the virtual world.
	 *
	 * @param image the image resource
	 */
	public void drawImage(Image image);
	
	public void zoomIn();
	public void zoomOut();
	public void resetZoom();
	

}
