package behaviors;

import java.util.Random;
import simulation.Simulator;
import simulation.robot.*;
import simulation.robot.behaviors.Behavior;
import simulation.util.Arguments;

public class WalkInCirclesBehavior extends Behavior {
	
	private double left;
	private double right;

	public WalkInCirclesBehavior(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		left = args.getArgumentAsDoubleOrSetDefault("left", 0.1);
		right = args.getArgumentAsDoubleOrSetDefault("right", 0.1);
	}

	@Override
	public void controlStep(double time) {
		DifferentialDriveRobot r = (DifferentialDriveRobot) robot;
		r.setWheelSpeed(left,right);
	}
}
