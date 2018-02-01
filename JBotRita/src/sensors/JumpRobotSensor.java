package sensors;

import java.util.Arrays;

import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;



/**
 * Similar to RobotSensor, but allowing to increase the range when the robot is a JumpingRobot and is currently jumping
 * @author Rita Ramos
 */


public class JumpRobotSensor extends RobotSensor {
	protected boolean rangedIncreased=false;
	@ArgumentsAnnotation(name = "increaseRange", help = "Increase range of the sensor while jumping.", defaultValue = "1.0")
	protected double increaseRange = 1.0;
	
	public JumpRobotSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		increaseRange = (args.getArgumentIsDefined("increaseRange")) ? args
				.getArgumentAsDouble("increaseRange") : 1.0;
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		if (((JumpingRobot) robot).isJumping() && !rangedIncreased){
			range+= increaseRange;
			rangedIncreased=true;
		}
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
		if ((sensorInfo.getDistance() < getCutOff())
				&& (sensorInfo.getAngle() < (openingAngle / 2.0))
				&& (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			double reading= (getRange() - sensorInfo.getDistance()) / getRange();
			rangeBackToDefault();
			return reading; 
		}
		rangeBackToDefault();
		return 0;
	}

	
	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "JumpingRobotPositionSensor [readings=" + Arrays.toString(readings)
				+ "]";
	}

	protected void rangeBackToDefault(){
		if( rangedIncreased==true){
			rangedIncreased=false;
			range=range-increaseRange;
		}
	}
}
