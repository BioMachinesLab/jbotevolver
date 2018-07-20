package sensors;

import java.util.ArrayList;

import net.jafama.FastMath;
import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 * Alignment sensor to see the relative orientation regarding others robots
 * (average of the neighbours robots orientation)
 * similar to rbg sensor
 * @author Rita Ramos
 */

public class RotationRobots2GlobalSensor extends Sensor {
	private Simulator simulator;
	private Robot robot;
	protected boolean rangedIncreased = false;
	protected double reading=0.0;
	protected ClosePhysicalObjects closeObjects;
	protected GeometricCalculator geoCalc;
	protected Vector2d 	sensorPosition 	= new Vector2d();
	private Environment env=null;

	@ArgumentsAnnotation(name = "range", help = "Range of the sensor.", defaultValue = "1.0")
	protected double range = 1.0;

	@ArgumentsAnnotation(name = "increaseRange", help = "Increase range of the sensor while jumping.", defaultValue = "1.0")
	protected double increaseRange = 1.0;

	public RotationRobots2GlobalSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.geoCalc = new GeometricCalculator();
		this.simulator = simulator;
		this.env = simulator.getEnvironment();
		this.robot = robot;
		range = (args.getArgumentIsDefined("range")) ? args
				.getArgumentAsDouble("range") : 1.0;
		increaseRange = (args.getArgumentIsDefined("increaseRange")) ? args
				.getArgumentAsDouble("increaseRange") : 1.0;
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));

	}

	public void update(double time, ArrayList<PhysicalObject> teleported) {
		sensorPosition.set(robot.getPosition().getX(), robot.getPosition().getY());
		if(closeObjects != null){
			closeObjects.update(time, teleported);

			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if(source.getObject().isInvisible())
					continue;
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
					iterator.updateCurrentDistance(this.geoCalc.getDistanceBetween(sensorPosition, source.getObject(), time));
				}
			}
		}
	}
	
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		Vector2d robotPosition = robot.getPosition();
		double sumRelativeOrientation = 0.0;
		double numberOfRobots_withinRange = 0.0;
		if(robot instanceof JumpingRobot){
			if (((JumpingRobot) robot).isJumping()){
				range+=increaseRange;
				rangedIncreased = true;
			}
		}
		for (Robot robotNeighbour : simulator.getRobots()) {

			if (robotPosition.distanceTo(robotNeighbour.getPosition()) < range) {

				if (robot.getId() != robotNeighbour.getId()) {
					double differenceOfOrientation = calculateDifferenceBetweenAngles(
							Math.toDegrees(robotNeighbour.getOrientation()),
							Math.toDegrees(robot.getOrientation()));

					sumRelativeOrientation += 0.5 * (differenceOfOrientation) / 180 + 0.5;
					numberOfRobots_withinRange += 1.0;

				}
			}
		}
		rangeBackToDefault();
		if (numberOfRobots_withinRange > 0.0)
			reading= sumRelativeOrientation / numberOfRobots_withinRange; 
		else
			reading= 0.0;
	}
	
	
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return reading;
	}

	private double calculateDifferenceBetweenAngles(double secondAngle,
			double firstAngle) {
		double difference = secondAngle - firstAngle;
		while (difference < -180)
			difference += 360;
		while (difference > 180)
			difference -= 360;
		return difference;
	}

	@Override
	public String toString() {
		return "RotationRobotsGlobalSensor [" + getSensorReading(0) + "]";
	}
	
	private void rangeBackToDefault() {
		if (rangedIncreased == true) {
			rangedIncreased = false;
			range = range - increaseRange;
		}
	}
	
	public void setAllowedObjectsChecker(AllowedObjectsChecker aoc) {
		if(aoc != null)
			this.closeObjects 	= new ClosePhysicalObjects(env,range,aoc);
	}
	
	public ClosePhysicalObjects getCloseObjects() {
		return closeObjects;
	}
}
