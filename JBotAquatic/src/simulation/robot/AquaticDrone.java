package simulation.robot;

import java.util.ArrayList;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import objects.Entity;
import simulation.Simulator;
import simulation.robot.actuator.PropellersActuator;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.CompassSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;
import commoninterface.CISensor;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class AquaticDrone extends DifferentialDriveRobot implements AquaticDroneCI{

	private double frictionConstant = 0.21;//0.05
	private double accelarationConstant = 0.20;//0.1
	private Vector2d velocity = new Vector2d();
	private Simulator simulator;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<CISensor> cisensors = new ArrayList<CISensor>();
	private PropellersActuator propellers;
	private SimulatedBroadcastHandler broadcastHandler;
	
	@ArgumentsAnnotation(name="gpserror", defaultValue = "0.0")
	private double gpsError = 0;
	
	@ArgumentsAnnotation(name="compasserror", defaultValue = "0.0")
	private double compassError = 0;
	
	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.simulator = simulator;
		broadcastHandler = new SimulatedBroadcastHandler(this);
		gpsError = args.getArgumentAsDoubleOrSetDefault("gpserror", gpsError);
		compassError = args.getArgumentAsDoubleOrSetDefault("compasserror", compassError);
		
		sensors.add(new CompassSensor(simulator, sensors.size()+1, this, args));
		actuators.add(new PropellersActuator(simulator, actuators.size()+1, args));
	}
	
	@Override
	public void shutdown() {}

	public void setMotorSpeeds(double leftMotorPercentage, double rightMotorPercentage) {
		if(propellers == null)
			propellers = (PropellersActuator) getActuatorByType(PropellersActuator.class);
		
		propellers.setLeftPercentage(leftMotorPercentage);
		propellers.setRightPercentage(rightMotorPercentage);
		propellers.apply(this);
	}

	@Override
	public double getCompassOrientationInDegrees() {
		CompassSensor compassSensor = (CompassSensor) getSensorByType(CompassSensor.class);
		double heading = (360-(compassSensor.getSensorReading(0) * 360) + 90) % 360;
		return heading;
	}

	@Override
	public LatLon getGPSLatLon() {
		
		LatLon latLon = CoordinateUtilities.cartesianToGPS(getPosition().getX(), getPosition().getY());
		
		if(gpsError > 0) {
			double lat = latLon.getLat();
			double lon = latLon.getLon();

			//TODO check if this is right
			double radius = simulator.getRandom().nextDouble()*gpsError;
			double angle = simulator.getRandom().nextDouble()*Math.PI*2;

			lat+=radius*Math.sin(angle);
			lon+=radius*Math.cos(angle);
			
			return new LatLon(lat, lon);
		}
		
		return latLon;
	}
	
	@Override
	public double getGPSOrientationInDegrees() {
		// TODO how should we model this? add error? 
		return getCompassOrientationInDegrees();
	}

	@Override
	public double getTimeSinceStart() {
		return simulator.getTime()*10;
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
	
	private double motorModel(double d) {
		return 0.0048*Math.exp(2.4912*Math.abs(d*2)) - 0.0048;
	}
	
	@Override
	public void updateActuators(Double time, double timeDelta) {
		
		double lw = Math.signum(rightWheelSpeed-leftWheelSpeed);
		
		orientation = MathUtils.modPI2(orientation + motorModel(rightWheelSpeed-leftWheelSpeed)*lw);//timeDelta * (Math.log(Math.abs(rightWheelSpeed - leftWheelSpeed) + 1) * a * lw));
		
		double accelDirection = (rightWheelSpeed+leftWheelSpeed) < 0 ? -1 : 1;
		double lengthOfAcc = accelarationConstant * (leftWheelSpeed + rightWheelSpeed);
		
		//Backwards motion should be slower. This value here is just an
		//estimate, and should be improved by taking real world samples
		if(accelDirection < 0)
			lengthOfAcc*=0.2;
		
		Vector2d accelaration = new Vector2d(lengthOfAcc * FastMath.cosQuick(orientation), lengthOfAcc * FastMath.sinQuick(orientation));
		
		velocity.setX(velocity.getX() * (1 - frictionConstant));
		velocity.setY(velocity.getY() * (1 - frictionConstant));    
		
		velocity.add(accelaration);
		
		position.set(
				position.getX() + timeDelta * velocity.getX(), 
				position.getY() + timeDelta * velocity.getY());
			
		for (Actuator actuator : actuators) {
			actuator.apply(this);
		}
		
		broadcastHandler.update(time);
	}

	@Override
	public void begin(CIArguments args, CILogger logger) {
		
	}

	@Override
	public ArrayList<Entity> getEntities() {
		return entities;
	}
	
	@Override
	public ArrayList<CISensor> getCISensors() {
		return cisensors;
	}
	
	@Override
	public String getNetworkAddress() {
		return getId()+":"+getId()+":"+getId()+":"+getId();
	}
	
	@Override
	public BroadcastHandler getBroadcastHandler() {
		return broadcastHandler;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
}