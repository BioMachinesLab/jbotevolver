package behaviors;

import java.util.Random;
import behaviors.Behavior;
import simulation.Simulator;
import simulation.robot.*;
import simulation.util.Arguments;

public class RandomWalkBehavior extends Behavior {

	private Random random;
	private double left = 0;
	private double right = 0;
	
	public RandomWalkBehavior(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		this.random = simulator.getRandom();
	}

	@Override
	public void controlStep(double time) {
		DifferentialDriveRobot r = (DifferentialDriveRobot) robot;
		
		if(time % 10 == 0) {
			left = random.nextDouble()*0.1;
			right = random.nextDouble()*0.1;
		}
		r.setWheelSpeed(left,right);
	}
}
