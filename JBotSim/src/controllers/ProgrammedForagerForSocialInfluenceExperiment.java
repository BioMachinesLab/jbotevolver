package controllers;

import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.InNestSensor;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class ProgrammedForagerForSocialInfluenceExperiment extends Controller {

	// private static final int TURNSPEED = 300;

	protected static double MAXSPEED = Robot.MAXIMUMSPEED;
	protected static final double DELTA_SPEED = MAXSPEED / 10;
	protected static final float ALIGN_DELTA = 0.002f;

	protected static final int NUM_BACKWARD_STEPS = 5;
	protected static final int NUM_AVOID_STEPS = (int) (1 / DELTA_SPEED);
	protected static final int NUM_AVOID_STEPS_TURN = NUM_AVOID_STEPS / 2;
	protected static final float INIT_FACTOR_VALUE = 1;
	protected static final double INITIAL_CIRCLE_WHEN_LOST = 0.3;
	protected static final double MIN_NEST_SIGNAL = 0.8;
	protected static final int NUM_FULL_FORWARD_STEPS = 50;

	protected double moveForwardSpeed = MAXSPEED;

	protected ConeTypeSensor nestSensors;
	protected ConeTypeSensor foodSensors;
	protected PreyCarriedSensor tray;
	protected TwoWheelActuator motors;
	protected PreyPickerActuator hand;
	// private RobotColorActuator topLight;
	protected Sensor robotRedSensor;
	// private Sensor robotBlueSensor;
	protected InNestSensor inNestSensor;

	private double lastLightFrontMeasure = 0;

	enum Action {
		NA, TURN_LEFT, TURN_RIGHT, FORWORD, GRAB, RELEASE, TURN_RIGHT_AVOID, BACKWARD, LOST, FULL_FORWORD, MOVE_RIGHT, MOVE_LEFT, DETOUR_RIGHT, DETOUR_LEFT
	};

	protected Action previousAction = Action.NA;
	protected Action nextAction = Action.NA;
	protected int actionCounter = 0;
	protected float factor = INIT_FACTOR_VALUE;
	protected double multFactor = INITIAL_CIRCLE_WHEN_LOST;
	protected double totalNestReading;
	protected int numberForaged = 0;

	// private double onNest;
	protected double nearPreyDistance;
	protected double front_sensor_measure;
	protected double left_sensor_measure;
	protected double right_sensor_measure;
	protected double maxNextReading;
	protected ConeTypeSensor sensor;

	public ProgrammedForagerForSocialInfluenceExperiment(Simulator simulator,
			Robot robot, Arguments arguments) {
		super(simulator, robot);
		double speed = 0;
		moveForwardSpeed = arguments
				.getArgumentIsDefined("speedofpreprogrammedrobots") ? arguments
				.getArgumentAsDouble("speedofpreprogrammedrobots")
				: Robot.MAXIMUMSPEED;

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
		robotRedSensor = (robot.getSensorWithId(4));
		inNestSensor = (InNestSensor) robot.getSensorWithId(5);
		// }else if( mode.equalsIgnoreCase("green")) {
		// robotBlueSensor=(robot.getSensorWithId(5));
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
		// topLight = (RobotColorActuator) robot.getActuatorWithId(3);
		// }
		// }

		nearPreyDistance = (foodSensors.getRange() - robot.getRadius() - ((RoundForageEnvironment) simulator
				.getEnvironment()).getPrey().get(0).getDiameter())
				/ foodSensors.getRange();
		// onNest = (nestSensors.getRange() - ((RoundForageEnvironment)
		// Environment
		// .getInstance()).getNestRadius() / 2) / nestSensors.getRange();
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
		case DETOUR_RIGHT:
			action_successfull = detourRight(moveForwardSpeed);
			break;
		case DETOUR_LEFT:
			action_successfull = detourLeft(moveForwardSpeed);
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

	protected Action chooseAction() {
		Action newAction;
		// boolean allowedArea = insideAllowedArea();
		updateNestReadings();
		boolean isLoaded = tray.getSensorReading(0) == 1;
		if (isLoaded) {
			// DROP FOOD
			// topLight.turnRed();
			sensor = nestSensors;
			// System.out.println("NEST: ");
		} else {
			// GET FOOD
			// topLight.turnGreen();
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

		if (isLoaded && inNestSensor.isInNest()) { // near(nestSensors, onNest))
			// { // ||
			// sensor.get(2).getSensorReading(0)>=1)){
			// stopMotors();
			if (nestSensors.getSensorReading(0) >= nestSensors
					.getSensorReading(1)
					&& nestSensors.getSensorReading(0) >= nestSensors
							.getSensorReading(7))
				newAction = Action.RELEASE;
			else if (nestSensors.getSensorReading(7) > nestSensors
					.getSensorReading(1))
				newAction = Action.TURN_RIGHT;
			else
				newAction = Action.TURN_LEFT;
		} else if (!isLoaded && near(foodSensors, nearPreyDistance)
				&& previousAction != Action.GRAB) {
			// (front_sensor_measure>= NEAR_PREY_DISTANCE ||
			// left_sensor_measure >= NEAR_PREY_DISTANCE ||
			// right_sensor_measure >= NEAR_PREY_DISTANCE)){ // ||
			// sensor.get(2).getSensorReading(0)>=1)){
			// //stopMotors();

			newAction = Action.GRAB;

			// } else if (!isLoaded && front_sensor_measure > 0
			// && front_sensor_measure <= robotRedSensor.getSensorReading(0)) {
		} else if (front_sensor_measure > 0
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
			// } else if (totalNestReading <= MIN_NEST_SIGNAL + .1
			// && allSensorsZero(foodSensors)) {
			// newAction = Action.FULL_FORWORD;
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
		motors.setWheelSpeed(moveForwardSpeed,
				moveForwardSpeed * Math.min(.98, 1 - multFactor));
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
		motors.setWheelSpeed(0, 0);
	}

	protected void turnRight(double speed) {
		motors.setWheelSpeed(speed, -speed * .9);
	}

	protected boolean detourRight(double speed) {
		motors.setWheelSpeed(speed, speed * .5);
		return true;
	}

	protected boolean detourLeft(double speed) {
		motors.setWheelSpeed(speed * .5, speed);
		return true;
	}

	protected void turnLeft(double speed) {
		motors.setWheelSpeed(-speed * .9, speed);
	}

	protected boolean move() {
		if (right_sensor_measure < left_sensor_measure) {
			motors.setWheelSpeed(moveForwardSpeed
					/ (1 + 3 * (left_sensor_measure - right_sensor_measure)),
					moveForwardSpeed);
		} else {
			motors.setWheelSpeed(moveForwardSpeed, moveForwardSpeed
					/ (1 + 3 * (right_sensor_measure - left_sensor_measure)));
		}
		return true;
	}

	protected boolean moveForward() {
		factor = INIT_FACTOR_VALUE;

		double crashFactor = 1;
		if (sensor.getSensorReading(0) < robotRedSensor.getSensorReading(0)) {
			crashFactor -= robotRedSensor.getSensorReading(0);
		}

		motors.setWheelSpeed(moveForwardSpeed, moveForwardSpeed * crashFactor);
		// if(actionCounter++==2){
		// actionCounter=0;
		return true;
		// } else {
		// return false;
		// }
	}

	private boolean run() {
		factor = INIT_FACTOR_VALUE;

		motors.setWheelSpeed(-moveForwardSpeed, -moveForwardSpeed);
		return true;
	}

	protected boolean moveBackward() {
		motors.setWheelSpeed(-moveForwardSpeed, -moveForwardSpeed);
		if (++actionCounter == NUM_BACKWARD_STEPS) {
			actionCounter = 0;
			return true;
		} else {
			return false;
		}
	}

	protected boolean moveFullForward() {
		motors.setWheelSpeed(moveForwardSpeed, moveForwardSpeed);
		if (++actionCounter == NUM_FULL_FORWARD_STEPS) {
			actionCounter = 0;
			return true;
		} else {
			return false;
		}
	}

	protected boolean avoid() {
		// if (actionCounter < NUM_AVOID_STEPS_TURN) {
		// run();
		// } else {
		detourRight(moveForwardSpeed);
		// }
		if (++actionCounter == NUM_AVOID_STEPS_TURN) {// NUM_AVOID_STEPS) {
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
