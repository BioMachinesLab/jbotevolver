package simulation.robot;

import java.util.LinkedList;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import objects.Waypoint;
import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.CompassSensor;
import simulation.robot.sensors.WaypointSensor;
import simulation.util.Arguments;

import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;

public class AquaticDrone extends DifferentialDriveRobot implements AquaticDroneCI{

	private double inertiaConstant = 0.05;
	private double accelarationConstant = 0.1;
	private Vector2d velocity = new Vector2d();
	private Simulator simulator;
	
	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.simulator = simulator;
	}
	
	@Override
	public void shutdown() {
		
	}

	@Override
	public void setMotorSpeeds(double leftMotor, double rightMotor) {
		TwoWheelActuator twoWheelActuator = (TwoWheelActuator) getActuatorByType(TwoWheelActuator.class);
		twoWheelActuator.setWheelSpeed(leftMotor, rightMotor);
	}

	@Override
	public double getCompassOrientationInDegrees() {
		CompassSensor compassSensor = (CompassSensor) getSensorByType(CompassSensor.class);
		double heading = (360-(compassSensor.getSensorReading(0) * 360) + 90) % 360;
		System.out.println(heading);
		return heading;
	}

	@Override
	public double getGPSLatitude() {
		WaypointSensor waypointSensor = (WaypointSensor) getSensorByType(WaypointSensor.class);
		return waypointSensor.getSensorReading(0);
	}

	@Override
	public double getGPSLongitude() {
		WaypointSensor waypointSensor = (WaypointSensor) getSensorByType(WaypointSensor.class);
		return waypointSensor.getSensorReading(1);
	}

	@Override
	public double getTimeSinceStart() {
		return simulator.getTime();
	}

	@Override
	public void setLed(int index, LedState state) { 
		setLedState(state);
	}
	
	@Override
	public void updateActuators(Double time, double timeDelta) {
		if (stopTimestep <= 0) {

			orientation = MathUtils.modPI2(orientation + timeDelta * 0.5 / (distanceBetweenWheels / 2.0) * (rightWheelSpeed - leftWheelSpeed));
			double lengthOfAcc = accelarationConstant * Math.pow(((leftWheelSpeed + rightWheelSpeed) / 2.0),2);
			Vector2d accelaration = new Vector2d(lengthOfAcc * FastMath.cosQuick(orientation), lengthOfAcc * FastMath.sinQuick(orientation));
			
			velocity.setX(velocity.getX() * (1 - inertiaConstant));
			velocity.setY(velocity.getY() * (1 - inertiaConstant));
			
			velocity.add(accelaration);
			
			position.set(
					position.getX() + timeDelta * velocity.getX(), 
					position.getY() + timeDelta * velocity.getY());
			
		}

		stopTimestep--;

		for (Actuator actuator : actuators) {
			actuator.apply(this);
		}
	}

	@Override
	public void begin(String[] args, CILogger logger) {
		
	}

	@Override
	public LinkedList<Waypoint> getWaypoints() {
		return null;
	}

}
