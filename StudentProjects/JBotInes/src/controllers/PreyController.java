package controllers;

import java.util.Random;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.MouseSensor;
import simulation.util.Arguments;
import environment.OpenEnvironment;

public class PreyController extends Controller {

	private static final double CONTINUE_EXTRA_STEPS = 60;

	private static final double MAX_ROBOT_DISTANCE = 3;

	private double maxSpeed = 0.1;
	private Random random;
	private double continueExtraSteps = 0;
	private boolean continueForward = false;
	private boolean reset = true;
	private boolean specialBeahvior;

	private Simulator simulator;

	public PreyController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		this.simulator = simulator;
		specialBeahvior = false;
		if(args.getArgumentIsDefined("actuators")){
			Arguments actuators = new Arguments(args.getArgumentAsString("actuators"));
			for (int i = 0; i < actuators.getNumberOfArguments(); i++) {
				Arguments actuatorArgs = new Arguments(actuators.getValueAt(i));
				if(actuatorArgs.getArgumentIsDefined("maxspeed")){
					maxSpeed = actuatorArgs.getArgumentAsDoubleOrSetDefault("maxspeed", 0.1);
				}
			}
		}
		random = simulator.getRandom();
	}

	public PreyController(Simulator simulator, Robot robot, Arguments args, boolean specialBeahvior) {
		super(simulator, robot, args);
		this.simulator = simulator;
		this.specialBeahvior = specialBeahvior;
		if(args.getArgumentIsDefined("actuators")){
			Arguments actuators = new Arguments(args.getArgumentAsString("actuators"));
			for (int i = 0; i < actuators.getNumberOfArguments(); i++) {
				Arguments actuatorArgs = new Arguments(actuators.getValueAt(i));
				if(actuatorArgs.getArgumentIsDefined("maxspeed")){
					maxSpeed = actuatorArgs.getArgumentAsDoubleOrSetDefault("maxspeed", 0.1);
				}
			}
		}
		random = simulator.getRandom();
	}

	@Override
	public void controlStep(double time) {

		((DifferentialDriveRobot)robot).setWheelSpeed(0,0);

	}

}

