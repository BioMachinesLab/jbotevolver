package controllers;

import java.io.Serializable;
import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.Factory;

/**
 * Superclass for all the robot control logic. Subclasses should override at least the method {@link #controlStep(int)}
 * which is called at each control step. Subclasses can also override {@link #begin()} (called before a simulation is started), 
 * and {@link #end()} which is called after the simulation has ended.
 * 
 * @author alc
 */

public abstract class Controller extends SimulatorObject implements Serializable {
	/**
	 *  Robot controlled by this controller
	 */
	protected Robot   robot;

	/**
	 *  Control cycle period. The delay between subsequent calls to {@link #controlStep}.
	 */
	public float CONTROLCYCLEPERIOD = 0.1f;
	
	/**
	 *  Initialize a new controller for a {@link Robot}
	 */
	public Controller(Simulator simulator,Robot robot,Arguments args) {
		super();
		this.robot = robot;
	}
			
	/**
	 *  Called by the simulator just before the simulation starts (at time = 0)
	 */
	public void begin() {};

	/**
	 *  Called after the simulation has ended
	 */
	public void end() {};
	
	/**
	 *  Called if the simulation is temporarily interrupted
	 */
	public void pause() {};
	
	/**
	 *  Called if the state of the controller should be reseted
	 */
	public void reset() {};
	
	public static Controller getController(Simulator simulator, Robot robot, Arguments arguments) {

		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Controller 'name' not defined: "+arguments.toString());
		
		return (Controller)Factory.getInstance(arguments.getArgumentAsString("classname"),simulator,robot,arguments);
	}

	public void controlStep(double time, double d) {
		controlStep(time);
	}
	
	public void controlStep(double time) {}
}