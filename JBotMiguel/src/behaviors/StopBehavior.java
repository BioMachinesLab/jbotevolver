package behaviors;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.behaviors.Behavior;
import simulation.util.Arguments;

public class StopBehavior extends Behavior {

	public StopBehavior(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {
		
		DifferentialDriveRobot r = (DifferentialDriveRobot) robot;
		r.setWheelSpeed(0, 0);
	}
}