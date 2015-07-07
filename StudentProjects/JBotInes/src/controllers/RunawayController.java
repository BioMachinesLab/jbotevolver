package controllers;

import java.util.Random;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.MouseSensor;
import simulation.util.Arguments;
import environment.OpenEnvironment;

public class RunawayController extends Controller {

	private static final double CONTINUE_EXTRA_STEPS = 60;

	private static final double MAX_ROBOT_DISTANCE = 100;

	private double maxSpeed = 0.1;
	private Random random;
	private double direction = 1;
	private double extraSteps = 3;
	private double currentExtraSteps = 0;
	private double continueExtraSteps = 0;
	private boolean continueForward = false;
	private boolean reset = true;
	private boolean specialBeahvior;

	private Simulator simulator;

	public RunawayController(Simulator simulator, Robot robot, Arguments args) {
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
		direction = random.nextDouble() > 0.5 ? -1 : 1;
	}

	public RunawayController(Simulator simulator, Robot robot, Arguments args, boolean specialBeahvior) {
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
		direction = random.nextDouble() > 0.5 ? -1 : 1;
	}

	@Override
	public void controlStep(double time) {

		//Robot Sensor
		/*double frontRobotSensor = robot.getSensorWithId(2).getSensorReading(0);
		double leftRobotSensor = robot.getSensorWithId(2).getSensorReading(1);
		double rightRobotSensor = robot.getSensorWithId(2).getSensorReading(2);
		double backRobotSensor = robot.getSensorWithId(2).getSensorReading(3);

		double maxVal = Math.max(frontRobotSensor, leftRobotSensor);
		maxVal = Math.max(maxVal, rightRobotSensor);
		maxVal = Math.max(maxVal, backRobotSensor);

		if(maxVal > 0){
			continueForward = true;
			reset = true;

//			if(maxVal == frontRobotSensor){
//				((DifferentialDriveRobot)robot).setWheelSpeed(-maxSpeed, -maxSpeed);
//			} else if(maxVal == leftRobotSensor) {
//				((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed/2);
//			} else if(maxVal == rightRobotSensor) {
//				((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed/2, maxSpeed);
//			} else if(maxVal == backRobotSensor) {
//				((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed);
//			}


			((DifferentialDriveRobot)robot).setWheelSpeed(0, 0);

		}else{

			if(specialBeahvior){
				if (reset){
					continueExtraSteps = CONTINUE_EXTRA_STEPS;
					reset = false;
				}

				if (continueForward) {
					if(continueExtraSteps > 0){
						continueExtraSteps --;
						((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed);
					}else
						continueForward = false;

				}else{
					direction = random.nextDouble() > 0.5 ? -1 : 1;
					((DifferentialDriveRobot)robot).setWheelSpeed(0,0);
				}
			}else{
				boolean inRange = false;
				for(Robot r: simulator.getRobots()){
					if(!r.getDescription().equals("prey") && !r.getDescription().equals("type1")){
						double distance = r.getPosition().distanceTo(robot.getPosition());
						
						if(distance < ((MouseSensor) r.getSensorByType(MouseSensor.class)).getRange())
							inRange = true;
					}
				}
				
				double distance = robot.getPosition().distanceTo(((OpenEnvironment)simulator.getEnvironment()).getCenterOfMass());
				if(!inRange && distance > MAX_ROBOT_DISTANCE)
					robot.setPosition(((OpenEnvironment)simulator.getEnvironment()).newRandomPosition());
				else 
					((DifferentialDriveRobot)robot).setWheelSpeed(0,0);

			}

		} */
		
		((DifferentialDriveRobot)robot).setWheelSpeed(0,0);

	}

}