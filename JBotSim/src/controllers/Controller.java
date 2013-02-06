package controllers;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

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
	 *  Called just before the simulation starts (at time = 0)
	 */
	public abstract void controlStep(double time);

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

		String controllerName = arguments.getArgumentAsString("classname");
		
		try {
			Constructor<?>[] constructors = Class.forName(controllerName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 3 && params[0] == Simulator.class
						&& params[1] == Robot.class && params[2] == Arguments.class) {
					return (Controller) constructor.newInstance(simulator,robot,arguments);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		throw new RuntimeException("Unknown controller: " + controllerName);
	}
	
}