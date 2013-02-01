package controllers;

import java.util.Random;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class ProgrammedNeutralForagerForSocialInfluenceExperiment extends
		ProgrammedForagerForSocialInfluenceExperiment {

	private static final double SPEED_FRACTION = .5;
	private static double PROB_GRAB = .5;
	private static double PROB_RELEASE = .001;
	private SimRandom random;
	private double onLimitDistance;
	private double maxNestReading;
	private double grabLimit;

	public ProgrammedNeutralForagerForSocialInfluenceExperiment(Simulator simulator,Robot robot,
			Arguments arguments) {
		super(simulator,robot, arguments);
		double range = nestSensors.getRange();
		onLimitDistance = 1.02
				* (range - ((RoundForageEnvironment)simulator.getEnvironment())
						.getForageRadius()) / range;
		grabLimit = (range - .75 * ((RoundForageEnvironment) simulator.getEnvironment()).getForageRadius()) / range;
		moveForwardSpeed *= SPEED_FRACTION;
		this.random = simulator.getRandom();
	}

	protected Action chooseAction() {
		Action newAction;
		// LightTypeSensor sensor;

		updateNestReadings();

		boolean allowedArea = insideAllowedArea();
		// boolean allowedForagingArea = insideAllowedForagingArea();
		boolean isLoaded = tray.getSensorReading(0) == 1;

		if (isLoaded && random.nextDouble() < PROB_RELEASE) {
			newAction = Action.RELEASE;
		} else if (!isLoaded && maxNestReading > grabLimit
				&& near(foodSensors, nearPreyDistance)
				&& random.nextDouble() < PROB_GRAB) {
			newAction = Action.GRAB;
		} else {
			front_sensor_measure = nestSensors.getSensorReading(0);
			left_sensor_measure = nestSensors.getSensorReading(1);
			right_sensor_measure = nestSensors.getSensorReading(7);
			if (allSensorsZero(nestSensors)) {
				newAction = Action.LOST;
			} else if (!allowedArea) {
				if (right_sensor_measure == left_sensor_measure
						&& right_sensor_measure == front_sensor_measure
						&& right_sensor_measure == 0) {
					newAction = Action.TURN_RIGHT;
				} else if (right_sensor_measure > left_sensor_measure
						&& right_sensor_measure > front_sensor_measure) {
					newAction = Action.MOVE_RIGHT;
				} else if (right_sensor_measure < left_sensor_measure
						&& left_sensor_measure > front_sensor_measure) {
					newAction = Action.MOVE_LEFT;

				} else {
					newAction = Action.FORWORD;
				}
			} else if (random.nextInt(2) == 0) {
				newAction = Action.DETOUR_RIGHT;
			} else {
				newAction = Action.DETOUR_LEFT;
			}
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
		if (maxNestReading < onLimitDistance) {
			return false;
		} else {
			return true;
		}
	}

	private boolean insideAllowedForagingArea() {
		if (maxNestReading < grabLimit) {
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

	// private boolean turnRight() {
	// if (previousAction == Action.TURN_LEFT) {
	// // if (factor > 200)
	// // factor=0.5f;
	// // else
	// factor++;
	// }
	// turnRight(MOVEFORWARDSPEED / factor);
	// return true;
	// }
	//
	// private boolean turnLeft() {
	// if (previousAction == Action.TURN_RIGHT) {
	// // if (factor > 200)
	// // factor=0.5f;
	// // else
	// factor++;
	// }
	// if (factor > 20)
	// turnLeft(MOVEFORWARDSPEED);
	// else
	// turnLeft(MOVEFORWARDSPEED / factor);
	// return true;
	// }
	//
	// private boolean lost() {
	// multFactor *= .995;
	// motors.setWheelSpeed(MAXSPEED, MAXSPEED * Math.min(.98, 1 - multFactor));
	// // if(++actionCounter==NUM_AVOID_STEPS){
	// // actionCounter=0;
	// return true;
	// // } else {
	// // return false;
	// // }
	// }

	//
	// private boolean garb() {
	// grabFood();
	// factor = INIT_FACTOR_VALUE;
	// // System.out.println("grabbing...");
	// return true;
	// }
	//
	// private boolean release() {
	// releaseFood();
	// factor = INIT_FACTOR_VALUE;
	// // System.out.println("releasing...");
	// return true;
	// }
	//
	// private void releaseFood() {
	// hand.drop();
	// numberForaged++;
	// }
	//
	// private void grabFood() {
	// hand.pick();
	// }
	//
	// @Override
	// public void end() {
	// }
	//
	// void stopMotors() {
	// motors.setWheelSpeed(0, 0);
	// }
	//
	// private void turnRight(double speed) {
	// motors.setWheelSpeed(speed, -speed * .9);
	// }
	//
	// private void turnLeft(double speed) {
	// motors.setWheelSpeed(-speed * .9, speed);
	// }
	//
	protected boolean moveForward() {
		factor = INIT_FACTOR_VALUE;

		motors.setWheelSpeed(moveForwardSpeed, moveForwardSpeed);
		// if(actionCounter++==2){
		// actionCounter=0;
		return true;
		// } else {
		// return false;
		// }
	}
	//
	// private boolean run() {
	// factor = INIT_FACTOR_VALUE;
	//
	// motors.setWheelSpeed(-MAXSPEED, -MAXSPEED);
	// return true;
	// }
	//
	// private boolean moveBackward() {
	// motors.setWheelSpeed(-MOVEFORWARDSPEED, -MOVEFORWARDSPEED);
	// if (++actionCounter == NUM_BACKWARD_STEPS) {
	// actionCounter = 0;
	// return true;
	// } else {
	// return false;
	// }
	// }
	//
	// private boolean moveFullForward() {
	// motors.setWheelSpeed(MOVEFORWARDSPEED, MOVEFORWARDSPEED);
	// if (++actionCounter == NUM_FULL_FORWARD_STEPS) {
	// actionCounter = 0;
	// return true;
	// } else {
	// return false;
	// }
	// }
	//
	// private boolean avoid() {
	// if (actionCounter < NUM_AVOID_STEPS_TURN) {
	// run();
	// } else {
	// turnRight(MAXSPEED);
	// }
	// if (++actionCounter == NUM_AVOID_STEPS) {
	// actionCounter = 0;
	// return true;
	// } else {
	// return false;
	// }
	//
	// }
	//
	// public int getNumberForaged() {
	// return numberForaged;
	// }

}
