package simulation.robot.sensors;

import java.util.Arrays;
import java.util.Random;

import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class LightTypeSensor extends ConeTypeSensor{
	private static final double NOISESTDEV = 0.01;
	private static final double MIN_DISTANCE = 0.01;
	private static final double MIN_READING = 1/MIN_DISTANCE;
	
	protected double openingAngle;
	protected double maxDistance;	
	protected double r;

	protected Random random;
	
	public LightTypeSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.random = simulator.getRandom();
		double openingAngle = (args.getArgumentIsDefined("angle")) ? FastMath.toRadians(args.getArgumentAsDouble("angle")): DEFAULT_OPENING_ANGLE;
		
		this.openingAngle = openingAngle/2;
		maxDistance = range - robot.getRadius();
		r=(Math.PI/2)/openingAngle;
		
		if(numberOfSensors == 2) {
			angles[0] = Math.PI/2.0;
			angles[1] = (3.0*Math.PI)/2.0;
		}
		
		setAllowedObjectsChecker(new AllowLightChecker());
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
		if(sensorInfo.getDistance()<getRange() && (sensorInfo.getAngle()< (openingAngle)) &&
				(sensorInfo.getAngle()>(-openingAngle))){

			//double currentMaxDistance = maxDistance - source.getObject().getRadius();
			
//			double distance=Math.max(sensorInfo.getDistance(),MIN_DISTANCE);	
			//return (currentMaxDistance - distance) / currentMaxDistance * (openingAngle - Math.abs(sensorInfo.getAngle())) / openingAngle;
				
//			double val=1/distance*Math.cos((sensorInfo.getAngle())*r)/MIN_READING;
//			return val;
			double val = ((getRange()-sensorInfo.getDistance())/getRange())*FastMath.cosQuick((sensorInfo.getAngle())*r) + 
					random.nextGaussian() * NOISESTDEV;
			if (val > 1.0)
				val = 1.0;
			else if (val < 0.0)
				val = 0.0;
				
			return val;
		}
 		return 0;
	}


	@Override
	public String toString() {
		for(int i=0;i<numberOfSensors;i++)
			getSensorReading(i);
		return "SimPointLightSensor [readings=" + Arrays.toString(readings)
				+ "]";
	}
}