package simulation.robot.sensors;

import java.awt.Color;
import java.util.Arrays;

import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class RobotColorSensor extends ConeTypeSensor {
	private double r;
	private Color mode;

	public static final float NOISESTDEV = 0.05f; 

	public RobotColorSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		String modeStr = (args.getArgumentIsDefined("mode")) ? args.getArgumentAsString("mode") : "red";
		this.openingAngle = openingAngle/2;
		r=(Math.PI/2)/openingAngle;
		
		if (modeStr.equalsIgnoreCase("red")){
			mode = Color.red;
		} else if (modeStr.equalsIgnoreCase("green")){
			mode = Color.green;
		}
		
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);

		if(sensorInfo.getDistance()<getRange() && (sensorInfo.getAngle()< (openingAngle)) && 
				(sensorInfo.getAngle()>(-openingAngle))){
			double distance=(sensorInfo.getDistance());	
			//return (currentMaxDistance - distance) / currentMaxDistance * (openingAngle - Math.abs(sensorInfo.getAngle())) / openingAngle;
			return	1.0/(1+distance*distance)*FastMath.cosQuick((sensorInfo.getAngle())*r);
//			double distance=sensorInfo.getDistance();	
//			return 1/distance/distance*Math.cos((sensorInfo.getAngle())*r);
		}
		return 0;
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		Robot nextRobot = (Robot) source.getObject();
		for(int j=0;j<numberOfSensors;j++){
			if((mode==null && nextRobot.getBodyColor().getRGB() != Color.BLACK.getRGB())||
					nextRobot.getBodyColor().getRGB()==mode.getRGB() ){
				readings[j]+=calculateContributionToSensor(j,source);
			}
		}
	}

	@Override
	public int getNumberOfSensors() {
		return numberOfSensors;
	}

	@Override
	public String toString() {
		for(int i=0;i<numberOfSensors;i++)
			getSensorReading(i);
		return "RobotColorSensor [readings=" + Arrays.toString(readings) + "]";
	}

}
