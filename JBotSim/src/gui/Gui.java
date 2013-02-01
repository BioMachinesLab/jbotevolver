package gui;

import simulation.Simulator;
import experiments.Experiment;
import gui.renderer.Renderer;

/**
 * Interface for all GUIs. A GUI has control over a simulation run. A GUI need not display any windows. 
 * A {@link Renderer} is passed to the {@link run} method. The {@link Renderer} is responsible for displaying 
 * the virtual world. A GUI implementation can choose to ignore the {@link Renderer} provided and use a specific one (or even 
 * two or more {@link Renderer}s at the same time).
 * 
 * @author alc
 */

public interface Gui {

	/**
	 * Runs one sample of a given number of simulation steps. 
	 * 
	 * @param simulator    the simulator configured with the correct robots, environment etc. 
	 * @param renderer     the suggested renderer for displaying the virtual world, statistics and/or save simulation data to a file. 
	 *                     A GUI implementation is free to ignore this argument and use one or more prespecified renderers.
	 * @param maxNumberOfSteps   the maximum number of simulation steps to run.
	 */
	public void run(Simulator simulator, Renderer renderer,int maxNumberOfSteps);

	/**
	 * Dispose of any windows, dialogs and other resources allocated by the GUI.
	 */
	public void dispose();
}
