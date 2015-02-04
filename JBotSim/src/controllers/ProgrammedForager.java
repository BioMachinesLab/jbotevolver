package controllers;

import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class ProgrammedForager extends Controller {

	// private static final int TURNSPEED = 300;

	private static final double MAXSPEED = Robot.MAXIMUMSPEED;
	private static final double DELTA_SPEED = MAXSPEED / 10;
	private static final float ALIGN_DELTA = 0.002f;

	private static final int NUM_BACKWARD_STEPS = 5;
	private static final int NUM_AVOID_STEPS = (int) (1 / DELTA_SPEED);
	private static final int NUM_AVOID_STEPS_TURN = NUM_AVOID_STEPS / 3;
	private static final float INIT_FACTOR_VALUE = 1;
	private static final double INITIAL_CIRCLE_WHEN_LOST = 0.3;
	private static final double MIN_NEST_SIGNAL = 0.8;
	private static final int NUM_FULL_FORWARD_STEPS = 50;

	private double moveForwardSpeed = 1.0;

	private ConeTypeSensor nestSensors;
	private ConeTypeSensor foodSensors;
	private PreyCarriedSensor tray;
	private TwoWheelActuator motors;
	private PreyPickerActuator hand;
	private RobotColorActuator topLight;
	private ConeTypeSensor robotRedSensor;
	private ConeTypeSensor robotBlueSensor;

	private double lastLightFrontMeasure = 0;

	private enum Action {
		NA, TURN_LEFT, TURN_RIGHT, FORWORD, GRAB, RELEASE, TURN_RIGHT_AVOID, BACKWARD, LOST, FULL_FORWORD, MOVE_RIGHT, MOVE_LEFT
	};

	private Action previousAction = Action.NA;
	private Action nextAction = Action.NA;
	private int actionCounter = 0;
	private float factor = INIT_FACTOR_VALUE;
	private double multFactor = INITIAL_CIRCLE_WHEN_LOST;
	private double totalNestReading;
	private int numberForaged = 0;

	private double onNest;
	private double nearPreyDistance;
	private double front_sensor_measure;
	private double left_sensor_measure;
	private double right_sensor_measure;
	private double maxNextReading;

	public ProgrammedForager(Simulator simulator, Robot robot, Arguments arguments) {
		super(simulator, robot, arguments);
		double speed = 0;
		moveForwardSpeed = arguments
				.getArgumentIsDefined("speedofpreprogrammedrobots") ? arguments
				.getArgumentAsDouble("speedofpreprogrammedrobots") : 1;

		// Arguments inputs= new
		// Arguments(arguments.getArgumentAsString("inputs"));

		// for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
		// Arguments sensor_arguments = new Arguments(inputs.getValueAt(i));
		// int id = -1;
		// if (sensor_arguments.getArgumentIsDefined("id"))
		// id = sensor_arguments.getArgumentAsInt("id");
		// String name=inputs.getArgumentAt(i);
		// if( name.equalsIgnoreCase("nestsensor")) {
		nestSensors = (ConeTypeSensor) (robot.getSensorWithId(1));
		// }else if (name.equalsIgnoreCase("preysensor")) {
		foodSensors = (ConeTypeSensor) (robot.getSensorWithId(2));
		// }else if (name.equalsIgnoreCase("preycarried")) {
		tray = (PreyCarriedSensor) robot.getSensorWithId(3);
		// }else if( name.equalsIgnoreCase("robotcolor")) {
		// String mode = sensor_arguments.getArgumentAsString("mode");
		// if(mode.equalsIgnoreCase("red")){
		robotRedSensor = (ConeTypeSensor) (robot.getSensorWithId(4));
		// }else if( mode.equalsIgnoreCase("green")) {
		robotBlueSensor = (ConeTypeSensor) (robot.getSensorWithId(5));
		// }
		// }
		// }

		// Arguments outputs= new
		// Arguments(arguments.getArgumentAsString("outputs"));
		// for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
		// Arguments actuator_arguments = new Arguments(outputs.getValueAt(i));
		// int id = -1;
		// if (actuator_arguments.getArgumentIsDefined("id"))
		// id = actuator_arguments.getArgumentAsInt("id");
		// String name=outputs.getArgumentAt(i);
		// if( name.equalsIgnoreCase("twowheel")) {
		motors = (TwoWheelActuator) robot.getActuatorWithId(1);
		// }else if (name.equalsIgnoreCase("preypicker")){
		hand = (PreyPickerActuator) robot.getActuatorWithId(2);
		// }else if (name.equalsIgnoreCase("robotcolor")) {
		topLight = (RobotColorActuator) robot.getActuatorWithId(3);
		// }
		// }

		nearPreyDistance = (foodSensors.getRange() - robot.getRadius() - ((RoundForageEnvironment) simulator
				.getEnvironment()).getPrey().get(0).getDiameter())
				/ foodSensors.getRange();
		onNest = (nestSensors.getRange() - ((RoundForageEnvironment) simulator
				.getEnvironment()).getNestRadius() / 2)
				/ nestSensors.getRange();
	}

	@Override
	public void begin() {
	}

	@Override
	public void controlStep(double time) {

		if (nextAction == Action.NA) {
			nextAction = chooseAction();
		}
		boolean action_successfull = false;
		switch (nextAction) {
		case TURN_LEFT:
			action_successfull = turnLeft();
			break;
		case TURN_RIGHT:
			action_successfull = turnRight();
			break;
		case MOVE_LEFT:
			action_successfull = move();
			break;
		case MOVE_RIGHT:
			action_successfull = move();
			break;
		case FULL_FORWORD:
			action_successfull = moveFullForward();
			// System.out.println("Moving forward...");
			break;
		case FORWORD:
			action_successfull = moveForward();
			// System.out.println("Moving forward...");
			break;
		case BACKWARD:
			action_successfull = moveBackward();
			// System.out.println("Moving backward!!!");
			break;
		case GRAB:
			action_successfull = garb();
			break;
		case RELEASE:
			action_successfull = release();
			break;
		case TURN_RIGHT_AVOID:
			action_successfull = avoid();
			// System.out.println("Turn Right AVOID!!!");
			break;
		case LOST:
			action_successfull = lost();
			break;

		default:
			break;
		}

		previousAction = nextAction;

		if (action_successfull) {
			nextAction = Action.NA;
		}
	}

	private Action chooseAction() {
		Action newAction;
		ConeTypeSensor sensor;
		// boolean allowedArea = insideAllowedArea();
		updateNestReadings();
		boolean isLoaded = tray.getSensorReading(0) == 1;
		if (isLoaded) {
			// DROP FOOD
			topLight.turnRed();
			sensor = nestSensors;
			// System.out.println("NEST: ");
		} else {
			// GET FOOD
			topLight.turnGreen();
			if (maxNextReading < MIN_NEST_SIGNAL && totalNestReading > 0
					&& foodSensors.getSensorReading(0) == 0) {
				sensor = nestSensors;
			} else {
				sensor = foodSensors;
			}
			// System.out.println("FOOD: ");
		}
		// System.out.println(sensor.get(1).getSensorReading(0)+" , "+sensor.get(2).getSensorReading(0)+",  "+sensor.get(3).getSensorReading(0));
		front_sensor_measure = sensor.getSensorReading(0);
		left_sensor_measure = sensor.getSensorReading(1);// -robotRedSensor.get(2).getSensorReading(0);
		// System.out.println("-------------------------------");
		right_sensor_measure = sensor.getSensorReading(7);// -robotRedSensor.get(1).getSensorReading(0);

		if (isLoaded && near(nestSensors, onNest)) { // ||
														// sensor.get(2).getSensorReading(0)>=1)){
			// stopMotors();
			newAction = Action.RELEASE;
		} else if (!isLoaded && near(foodSensors, nearPreyDistance)
				&& previousAction != Action.GRAB) {
			// (front_sensor_measure>= NEAR_PREY_DISTANCE ||
			// left_sensor_measure >= NEAR_PREY_DISTANCE ||
			// right_sensor_measure >= NEAR_PREY_DISTANCE)){ // ||
			// sensor.get(2).getSensorReading(0)>=1)){
			// //stopMotors();

			newAction = Action.GRAB;

		} else if (!isLoaded && front_sensor_measure > 0
				&& front_sensor_measure <= robotRedSensor.getSensorReading(0)) {
			newAction = Action.TURN_RIGHT_AVOID;
			// } else if(allSensorsZero(sensor) && (totalNestReading >
			// MIN_NEST_SIGNAL || allSensorsZero(nestSensors))){
			// newAction= Action.LOST;
		} else if (allSensorsZero(sensor)
				&& (maxNextReading > MIN_NEST_SIGNAL || allSensorsZero(nestSensors))) {
			newAction = Action.FORWORD;
		} else if (right_sensor_measure == left_sensor_measure
				&& right_sensor_measure == front_sensor_measure
				&& right_sensor_measure == 0) {
			newAction = Action.TURN_RIGHT;
		} else if (right_sensor_measure > left_sensor_measure
				&& right_sensor_measure > front_sensor_measure) {
			newAction = Action.MOVE_RIGHT;
		} else if (right_sensor_measure < left_sensor_measure
				&& left_sensor_measure > front_sensor_measure) {
			newAction = Action.MOVE_LEFT;
		} else if (totalNestReading <= MIN_NEST_SIGNAL + .1
				&& allSensorsZero(foodSensors)) {
			newAction = Action.FULL_FORWORD;
		} else {
			newAction = Action.FORWORD;
		}

		// if(lastLightFrontMeasure!=0 &&
		// lastLightFrontMeasure==front_sensor_measure){
		// //The robot is bloked move backward
		// newAction= Action.BACKWARD;
		// }else {
		// double front_red_measure=robotRedSensor.getSensorReading(0);
		// if(!isLoaded && front_sensor_measure>0 &&
		// front_sensor_measure<=front_red_measure){
		// newAction= Action.TURN_RIGHT_AVOID;
		//
		// }else if((front_sensor_measure >
		// MIN_READING)&&//right_sensor_measure>MIN_READING &&
		// left_sensor_measure>MIN_READING)&&
		// right_sensor_measure-left_sensor_measure < ALIGN_DELTA &&
		// right_sensor_measure-left_sensor_measure > -ALIGN_DELTA ){
		// newAction= Action.FORWORD;
		// } else if((right_sensor_measure==0 && left_sensor_measure==0)||
		// (right_sensor_measure>left_sensor_measure)){// &&
		// // right_sensor_measure>MIN_READING)){
		// newAction= Action.TURN_RIGHT;
		// // System.out.println("Turning right...");
		// } else
		// {//if(foodSensors.get(1).getSensorReading(0)<foodSensors.get(2).getSensorReading(0)){
		// newAction= Action.TURN_LEFT;
		// // System.out.println("Turning left...");
		// }
		// // }
		// lastLightFrontMeasure=front_sensor_measure;
		// }

		// newAction= Action.TURN_RIGHT;
		if (newAction != Action.LOST)
			multFactor = INITIAL_CIRCLE_WHEN_LOST;
		return newAction;
	}

	private void updateNestReadings() {
		totalNestReading = 0;
		maxNextReading = 0;
		for (int i = 0; i < nestSensors.getNumberOfSensors(); i++) {
			double reading = nestSensors.getSensorReading(i);
			if (maxNextReading < reading) {
				maxNextReading = reading;
			}
			totalNestReading += reading;
		}
	}

	private boolean near(ConeTypeSensor workingSensor, double range) {
		for (int i = 0; i < workingSensor.getNumberOfSensors(); i++) {
			if (workingSensor.getSensorReading(i) > range)
				return true;
		}
		return false;
	}

	private boolean insideAllowedArea() {
		totalNestReading = 0;
		for (int i = 0; i < nestSensors.getNumberOfSensors(); i++) {
			totalNestReading += nestSensors.getSensorReading(i);
		}
		if (totalNestReading < MIN_NEST_SIGNAL) {
			return false;
		} else {
			return true;
		}
	}

	private boolean allSensorsZero(ConeTypeSensor sensor) {
		for (int i = 0; i < sensor.getNumberOfSensors(); i++) {
			if (sensor.getSensorReading(i) > 0) {
				return false;
			}
		}
		return true;
	}

	private boolean turnRight() {
		if (previousAction == Action.TURN_LEFT) {
			// if (factor > 200)
			// factor=0.5f;
			// else
			factor++;
		}
		turnRight(moveForwardSpeed / factor);
		return true;
	}

	private boolean turnLeft() {
		if (previousAction == Action.TURN_RIGHT) {
			// if (factor > 200)
			// factor=0.5f;
			// else
			factor++;
		}
		if (factor > 20)
			turnLeft(moveForwardSpeed);
		else
			turnLeft(moveForwardSpeed / factor);
		return true;
	}

	private boolean lost() {
		multFactor *= .995;
		motors.setLeftWheelSpeed(1.0);
		motors.setRightWheelSpeed(Math.min(.98, 1 - multFactor));
		// if(++actionCounter==NUM_AVOID_STEPS){
		// actionCounter=0;
		return true;
		// } else {
		// return false;
		// }
	}

	private boolean garb() {
		grabFood();
		factor = INIT_FACTOR_VALUE;
		// System.out.println("grabbing...");
		return true;
	}

	private boolean release() {
		releaseFood();
		factor = INIT_FACTOR_VALUE;
		// System.out.println("releasing...");
		return true;
	}

	private void releaseFood() {
		hand.drop();
		numberForaged++;
	}

	private void grabFood() {
		hand.pick();
	}

	@Override
	public void end() {
	}

	void stopMotors() {
		motors.setLeftWheelSpeed(0.5);
		motors.setRightWheelSpeed(0.5);
	}

	private void turnRight(double speed) {
		motors.setLeftWheelSpeed(speed);
		motors.setRightWheelSpeed(-speed * .9);
	}

	private void turnLeft(double speed) {
		motors.setLeftWheelSpeed(-speed * .9);
		motors.setRightWheelSpeed(speed);
	}

	private boolean move() {
		if (right_sensor_measure < left_sensor_measure) {
			motors.setLeftWheelSpeed(moveForwardSpeed / (1 + (left_sensor_measure - right_sensor_measure)));
			motors.setRightWheelSpeed(moveForwardSpeed);
		} else {
			motors.setLeftWheelSpeed(moveForwardSpeed);
			motors.setRightWheelSpeed(moveForwardSpeed
					/ (1 + (right_sensor_measure - left_sensor_measure)));
		}
		return true;
	}

	private boolean moveForward() {
		factor = INIT_FACTOR_VALUE;
		
		motors.setLeftWheelSpeed(moveForwardSpeed);
		motors.setRightWheelSpeed(moveForwardSpeed);

		// if(actionCounter++==2){
		// actionCounter=0;
		return true;
		// } else {
		// return false;
		// }
	}

	private boolean run() {
		factor = INIT_FACTOR_VALUE;
		
		motors.setLeftWheelSpeed(0);
		motors.setRightWheelSpeed(0);

		return true;
	}

	private boolean moveBackward() {
		motors.setLeftWheelSpeed(0);
		motors.setRightWheelSpeed(0);
		if (++actionCounter == NUM_BACKWARD_STEPS) {
			actionCounter = 0;
			return true;
		} else {
			return false;
		}
	}

	private boolean moveFullForward() {
		motors.setLeftWheelSpeed(moveForwardSpeed);
		motors.setRightWheelSpeed(moveForwardSpeed);
		if (++actionCounter == NUM_FULL_FORWARD_STEPS) {
			actionCounter = 0;
			return true;
		} else {
			return false;
		}
	}

	private boolean avoid() {
		if (actionCounter < NUM_AVOID_STEPS_TURN) {
			run();
		} else {
			turnRight(1.0);
		}
		if (++actionCounter == NUM_AVOID_STEPS) {
			actionCounter = 0;
			return true;
		} else {
			return false;
		}

	}

	public int getNumberForaged() {
		return numberForaged;
	}

}
