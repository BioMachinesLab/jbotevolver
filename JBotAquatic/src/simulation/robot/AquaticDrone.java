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
import commoninterface.utils.CoordinateUtilities;

public class AquaticDrone extends DifferentialDriveRobot implements AquaticDroneCI{

	private double inertiaConstant = 0.05;
	private double accelarationConstant = 0.1;
	private Vector2d velocity = new Vector2d();
	private Simulator simulator;
//	private double lat = 38.749365;
//	private double lon = -9.153418;
	private LinkedList<Waypoint> waypoints = new LinkedList<>();
	
	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.simulator = simulator;

//		double[] start = CoordinateUtilities.GPSToCartesian(lat, lon);
//		System.out.println(start[0]+" "+start[1]);
//		setPosition(start[0], start[1]);
//		waypoints.add(new Waypoint(38.74957,-9.153383));
	}
	
	@Override
	public void shutdown() {}

	@Override
	public void setMotorSpeeds(double leftMotor, double rightMotor) {
		TwoWheelActuator twoWheelActuator = (TwoWheelActuator) getActuatorByType(TwoWheelActuator.class);
		twoWheelActuator.setWheelSpeed(leftMotor, rightMotor);
	}

	//Create drone compass sensor
	@Override
	public double getCompassOrientationInDegrees() {
		CompassSensor compassSensor = (CompassSensor) getSensorByType(CompassSensor.class);
		double heading = (360-(compassSensor.getSensorReading(0) * 360) + 90) % 360;
		return heading;
	}

	@Override
	public double getGPSLatitude() {
		return CoordinateUtilities.cartesianToGPS(getPosition().getX(), getPosition().getY())[0];
	}

	@Override
	public double getGPSLongitude() {
		return CoordinateUtilities.cartesianToGPS(getPosition().getX(), getPosition().getY())[1];
	}

	@Override
	public double getTimeSinceStart() {
		return simulator.getTime();
	}
	
	@Override
	public void setLed(int index, commoninterface.LedState state) {
		LedState robotState;
		
		switch(state) {
			case BLINKING:
				robotState = LedState.BLINKING;
				break;
			case OFF:
				robotState = LedState.OFF;
				break;
			case ON:
				robotState = LedState.ON;
				break;
			default:
				robotState = LedState.OFF;
		}
		
		setLedState(robotState);
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
		return waypoints;
	}

}
