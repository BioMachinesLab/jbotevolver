package controllers;

import java.util.Random;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class RandomWallRobotController extends Controller {
	
	private static final double CONTINUE_EXTRA_STEPS = 60;
	
	private double maxSpeed = 0.1;
	private Random random;
	private double direction = 1;
	private double extraSteps = 3;
	private double currentExtraSteps = 0;
	private double continueExtraSteps = 0;
	private boolean continueForward = false;
	private boolean reset = true;
	private boolean specialBeahvior;
	
	public RandomWallRobotController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
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
		direction = random.nextDouble() > 0.5 ? -1 : 1;
	}

	public RandomWallRobotController(Simulator simulator, Robot robot, Arguments args, boolean specialBeahvior) {
		super(simulator, robot, args);
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
		direction = random.nextDouble() > 0.5 ? -1 : 1;
	}

	@Override
	public void controlStep(double time) {
		
		//Wall Sensor
		double rightSpeed = robot.getSensorWithId(1).getSensorReading(0);
		double leftSpeed  = robot.getSensorWithId(1).getSensorReading(1);
		
		//Robot Sensor
		double frontRobotSensor = robot.getSensorWithId(2).getSensorReading(0);
		double leftRobotSensor = robot.getSensorWithId(2).getSensorReading(1);
		double rightRobotSensor = robot.getSensorWithId(2).getSensorReading(2);
		double backRobotSensor = robot.getSensorWithId(2).getSensorReading(3);
		  
		double maxVal = Math.max(frontRobotSensor, leftRobotSensor);
		maxVal = Math.max(maxVal, rightRobotSensor);
		maxVal = Math.max(maxVal, backRobotSensor);
		
		if(maxVal > 0){
			continueForward = true;
			reset = true;
			
			double val = Math.max(rightSpeed, leftSpeed);
			val = Math.max(val, frontRobotSensor);
			if(val > 0) {
				((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed*direction, -maxSpeed*direction);
				currentExtraSteps = extraSteps;
			} else {
				if(currentExtraSteps > 0) {
					currentExtraSteps--;
					((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed*direction, -maxSpeed*direction);
				}
				else{
					((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed);
				}
			}
		}else{
			
			if(specialBeahvior){
				if (reset){
					continueExtraSteps = CONTINUE_EXTRA_STEPS;
					reset = false;
				}
					
				if (continueForward) {
					if(continueExtraSteps > 0){
						continueExtraSteps --;
						double val = Math.max(rightSpeed, leftSpeed);
						val = Math.max(val, frontRobotSensor);
						if(val > 0) {
							((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed*direction, -maxSpeed*direction);
							currentExtraSteps = extraSteps;
						} else {
							if(currentExtraSteps > 0) {
								currentExtraSteps--;
								((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed*direction, -maxSpeed*direction);
							}
							else{
								((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed);
							}
						}
					}else
						continueForward = false;
		
				}else{
					direction = random.nextDouble() > 0.5 ? -1 : 1;
					((DifferentialDriveRobot)robot).setWheelSpeed(0,0);
				}
			}else{
				((DifferentialDriveRobot)robot).setWheelSpeed(0,0);
			}
			
		}
		
	}
	
}