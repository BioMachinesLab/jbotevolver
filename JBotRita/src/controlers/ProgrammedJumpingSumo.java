package controlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import actuator.IntensityPreyPickerActuator;
import actuator.JumpSumoActuator;
import controllers.Controller;
import environments_JumpingSumoIntensityPreys.JS_Environment;

public class ProgrammedJumpingSumo extends Controller {

	private static final float INIT_FACTOR_VALUE = 1;
	private double moveForwardSpeed = 1.9;
	private ConeTypeSensor foodSensors;
	private IntensityPreyPickerActuator pickUpPrey;
	private ConeTypeSensor wallSensor;
	private TwoWheelActuator motors;
	private JumpSumoActuator jumpingActuator;
	private boolean isToJump = true;
	private boolean firstTime = false;
	private float factor = INIT_FACTOR_VALUE;
	private int numberForaged = 0;
	private Random random;
	private Robot r;
	private Simulator simulator;

	public ProgrammedJumpingSumo(Simulator simulator, Robot robot,
			Arguments arguments) {
		super(simulator, robot, arguments);
		double speed = 0;
		moveForwardSpeed = arguments
				.getArgumentIsDefined("speedofpreprogrammedrobots") ? arguments
				.getArgumentAsDouble("speedofpreprogrammedrobots") : 1.9;

		wallSensor = (ConeTypeSensor) (robot.getSensorWithId(3));
		foodSensors = (ConeTypeSensor) (robot.getSensorWithId(2));
		motors = (TwoWheelActuator) robot.getActuatorWithId(1);
		jumpingActuator = (JumpSumoActuator) robot.getActuatorWithId(3);
		pickUpPrey = (IntensityPreyPickerActuator) robot.getActuatorWithId(2);
		random = simulator.getRandom();
		this.r = robot;
		this.simulator = simulator;

	}

	@Override
	public void begin() {

	}

	@Override
	public void controlStep(double time) {
		// System.out.println(wallSensor.getSensorReading(0)>=0.25);
		// System.out.println("walls"+ wallSensor.getSensorReading(0));
		// System.out.println("food"+ foodSensors.getSensorReading(0));

		if (firstTime = true) {
			r.setOrientation(0.25 * Math.PI * 2);
		}
		if (isToJump = false || foodSensors.getSensorReading(0) >= 0.5) {
			IntensityPrey prey = (IntensityPrey) simulator.getEnvironment()
					.getPrey().get(0);
			pickUpPrey.apply(r);
			pickUpPrey.dropPrey();
			if (prey.getIntensity() < 0) {
				simulator.stopSimulation();
			}
			isToJump = false;
		} else if (isToJump = true && wallSensor.getSensorReading(0) >= 0.25) {
			jump();
		} else {
			moveForward();
		}

	}

	@Override
	public void end() {
	}

	private boolean jump() {
		jumpingActuator.jump();
		jumpingActuator.apply(r);
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

	public int getNumberForaged() {
		return numberForaged;
	}

}
