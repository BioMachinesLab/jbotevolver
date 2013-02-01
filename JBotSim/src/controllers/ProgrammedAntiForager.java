package controllers;

import simulation.Controller;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class ProgrammedAntiForager extends Controller {

	// private static final int TURNSPEED = 300;

	private static final double MAXSPEED = Robot.MAXIMUMSPEED;
	private static final double DELTA_SPEED = MAXSPEED / 10;
	private static final double MOVEFORWARDSPEED = MAXSPEED;
	private static final float ALIGN_DELTA = 0.002f;

	private static final int NUM_BACKWARD_STEPS = 5;
	private static final int NUM_AVOID_STEPS = (int) (1 / DELTA_SPEED);
	private static final int NUM_AVOID_STEPS_TURN = NUM_AVOID_STEPS / 3;
	private static final float INIT_FACTOR_VALUE = 5;
	private static final double INITIAL_CIRCLE_WHEN_LOST = 0.3;
	private static final double MIN_NEST_SIGNAL = 2;
	private static final int NUM_FULL_FORWARD_STEPS = 50;

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
		NA, TURN_LEFT, TURN_RIGHT, FORWORD, GRAB, RELEASE, TURN_RIGHT_AVOID, BACKWARD, LOST, FULL_FORWORD
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
	private double onLimitDistance;
	private double maxNestReading;

	public ProgrammedAntiForager(Simulator simulator, Robot robot,
			Arguments arguments) {
		super(simulator, robot);
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
				.getEnvironment()).getNestRadius() / 2) / nestSensors.getRange();
		onLimitDistance = 0.75;
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
		LightTypeSensor sensor;

		updateNestReadings();

		boolean allowedArea = insideAllowedArea();
		boolean isLoaded = tray.getSensorReading(0) == 1;

		double front_sensor_measure;
		double left_sensor_measure;
		double right_sensor_measure;

		if (isLoaded) {
			// DROP FOOD
			topLight.turnRed();
			// System.out.println("NEST: ");
			front_sensor_measure = nestSensors.getSensorReading(4);
			left_sensor_measure = nestSensors.getSensorReading(5);
			right_sensor_measure = nestSensors.getSensorReading(3);

		} else {
			// GET FOOD
			topLight.turnGreen();
			if (maxNestReading < 0.8 && maxNestReading > 0) {
				front_sensor_measure = nestSensors.getSensorReading(0);
				left_sensor_measure = nestSensors.getSensorReading(1);
				right_sensor_measure = nestSensors.getSensorReading(7);
			} else {
				front_sensor_measure = foodSensors.getSensorReading(0);
				left_sensor_measure = foodSensors.getSensorReading(1);// -robotRedSensor.get(2).getSensorReading(0);
				right_sensor_measure = foodSensors.getSensorReading(7);// -robotRedSensor.get(1).getSensorReading(0);
			}
			// System.out.println("FOOD: ");

		}
		// System.out.println(sensor.get(1).getSensorReading(0)+" , "+sensor.get(2).getSensorReading(0)+",  "+sensor.get(3).getSensorReading(0));

		if (isLoaded && maxNestReading < onLimitDistance) { // ||
			// sensor.get(2).getSensorReading(0)>=1)){
			// stopMotors();
			newAction = Action.RELEASE;
		} else if (!isLoaded && maxNestReading > 0.75
				&& near(foodSensors, nearPreyDistance)) {

			if (previousAction != Action.GRAB) {
				// (front_sensor_measure>= NEAR_PREY_DISTANCE ||
				// left_sensor_measure >= NEAR_PREY_DISTANCE ||
				// right_sensor_measure >= NEAR_PREY_DISTANCE)){ // ||
				// sensor.get(2).getSensorReading(0)>=1)){
				// //stopMotors();

				newAction = Action.GRAB;
			} else {
				newAction = Action.FORWORD;
			}
			// } else if (!isLoaded && front_sensor_measure > 0
			// && front_sensor_measure <= robotRedSensor.getSensorReading(0)) {
			// newAction = Action.TURN_RIGHT_AVOID;

		} else if (allSensorsZero(foodSensors)
				&& (!allowedArea || allSensorsZero(nestSensors))) {
			newAction = Action.LOST;
		} else if (allFrontSensorsZero(foodSensors) && allowedArea) {
			newAction = Action.FORWORD;
		} else if (right_sensor_measure == left_sensor_measure
				&& right_sensor_measure == front_sensor_measure
				&& right_sensor_measure == 0) {
			newAction = Action.TURN_RIGHT;
		} else if (right_sensor_measure > left_sensor_measure
				&& right_sensor_measure > front_sensor_measure) {
			newAction = Action.TURN_RIGHT;
		} else if (right_sensor_measure < left_sensor_measure
				&& left_sensor_measure > front_sensor_measure) {
			newAction = Action.TURN_LEFT;
		} else if (!allowedArea && allSensorsZero(foodSensors)) {
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

	private boolean near(ConeTypeSensor workingSensor, double range) {
		for (int i = 0; i < workingSensor.getNumberOfSensors(); i++) {
			if (workingSensor.getSensorReading(i) > range)
				return true;
		}
		return false;
	}

	private void updateNestReadings() {
		totalNestReading = 0;
		maxNestReading = 0;
		for (int i = 0; i < nestSensors.getNumberOfSensors(); i++) {
			double reading = nestSensors.getSensorReading(i);
			if (maxNestReading < reading) {
				maxNestReading = reading;
			}
			totalNestReading += reading;
		}
	}

	private boolean insideAllowedArea() {
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

	private boolean allFrontSensorsZero(ConeTypeSensor sensor) {
		return (sensor.getSensorReading(0) == 0)
				&& (sensor.getSensorReading(1) == 0)
				&& (sensor.getSensorReading(7) == 0);
	}

	private boolean turnRight() {
		if (previousAction == Action.TURN_LEFT) {
			// if (factor > 200)
			// factor=0.5f;
			// else
			factor++;
		}
		turnRight(MOVEFORWARDSPEED / factor);
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
			turnLeft(MOVEFORWARDSPEED);
		else
			turnLeft(MOVEFORWARDSPEED / factor);
		return true;
	}

	private boolean lost() {
		multFactor *= .995;
		motors.setWheelSpeed(MAXSPEED, MAXSPEED * Math.min(.98, 1 - multFactor));
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

	private void turnRight(double speed) {
		motors.setWheelSpeed(speed, -speed * .9);
	}

	private void turnLeft(double speed) {
		motors.setWheelSpeed(-speed * .9, speed);
	}

	private boolean moveForward() {
		factor = INIT_FACTOR_VALUE;

		motors.setWheelSpeed(MOVEFORWARDSPEED, MOVEFORWARDSPEED);
		// if(actionCounter++==2){
		// actionCounter=0;
		return true;
		// } else {
		// return false;
		// }
	}

	private boolean run() {
		factor = INIT_FACTOR_VALUE;

		motors.setWheelSpeed(-MAXSPEED, -MAXSPEED);
		return true;
	}

	private boolean moveBackward() {
		motors.setWheelSpeed(-MOVEFORWARDSPEED, -MOVEFORWARDSPEED);
		if (++actionCounter == NUM_BACKWARD_STEPS) {
			actionCounter = 0;
			return true;
		} else {
			return false;
		}
	}

	private boolean moveFullForward() {
		motors.setWheelSpeed(MOVEFORWARDSPEED, MOVEFORWARDSPEED);
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
			turnRight(MAXSPEED);
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
