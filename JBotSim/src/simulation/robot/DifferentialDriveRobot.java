package simulation.robot;

import simulation.Simulator;

public class DifferentialDriveRobot extends Robot {
	/**
	 * Diameter of the robot's wheels.
	 */
	protected double     wheelDiameter      = 0.05;

	/**
	 * Current speed of the left wheel.
	 */	
	protected double     leftWheelSpeed     = 0;
	/**
	 * Current speed of the right wheel.
	 */	
	protected double     rightWheelSpeed    = 0;
	

	public DifferentialDriveRobot(Simulator simulator, String name, double x,
			double y, double orientation, double mass, double radius,
			double distanceWheels, String color) {
		super(simulator, name, x, y, orientation, mass, radius, distanceWheels, color);
	}

}
