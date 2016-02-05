package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SpinningController extends Controller{

	private Simulator simulator;
	private double maxSpeed;

	public SpinningController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		this.simulator = simulator;
		
		if(args.getArgumentIsDefined("actuators")){
			Arguments actuators = new Arguments(args.getArgumentAsString("actuators"));
			for (int i = 0; i < actuators.getNumberOfArguments(); i++) {
				Arguments actuatorArgs = new Arguments(actuators.getValueAt(i));
				if(actuatorArgs.getArgumentIsDefined("maxspeed")){
					maxSpeed = actuatorArgs.getArgumentAsDoubleOrSetDefault("maxspeed", 0.1);
				}
			}
		}
	}
	
	@Override
	public void controlStep(double time) {
		
		((DifferentialDriveRobot) robot).setWheelSpeed(-maxSpeed, maxSpeed);
	}

}
