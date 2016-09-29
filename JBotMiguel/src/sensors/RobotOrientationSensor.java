package sensors;

import java.util.ArrayList;
import java.util.Random;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class RobotOrientationSensor extends ConeTypeSensor {

	protected double r;

	public int slices;
	
	protected Random random;

	public RobotOrientationSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.random = simulator.getRandom();
		
		this.slices = numberOfSensors;
		this.openingAngle = openingAngle / 2;
		this.r = (Math.PI / 2) / openingAngle;

		readings = new double[slices];

		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
	}

	public void update(int time, ArrayList<PhysicalObject> teleported) {

		for (int j = 0; j < slices; j++) {
			readings[j] = 0.0;
		}

		super.update(time, teleported);
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {

		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
		if (sensorInfo.getDistance() < getRange()
				&& (sensorInfo.getAngle() < (openingAngle))
				&& (sensorInfo.getAngle() > (-openingAngle))) {
			
			double angle = angle(robot.getOrientation(),source.getObject().getOrientation());
			angle+=Math.PI;
			angle/=2*Math.PI;
			
			return angle;
		}
		return 0.5;
	}
	
	public double angle(double a, double b) {
		double i = b-a;
		
		if(Math.abs(i) > Math.PI) {
			if(i > 0)
				i=-(2*Math.PI-Math.abs(i));
			else
				i = (2*Math.PI-Math.abs(i));
		}
		return i;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	public double getDistance(int sensorNumber) {
		return readings[sensorNumber];
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
//		Robot nextRobot = (Robot) source.getObject();
		for (int j = 0; j < numberOfSensors; j++) {
			double newReading = calculateContributionToSensor(j, source) * (1 + random.nextGaussian() * NOISESTDEV);
			if (newReading > readings[j]) {
				readings[j] = newReading;
			}
		}
	}

	@Override
	public int getNumberOfSensors() {
		return slices;
	}

	protected String getArrayAsString(String label, double[] array) {
		String ret = "";
		int index = 0;

		for (double d : array) {
			if (index != 0) {
				ret += ", ";
			}
			ret += label + (index++) + ": " + d;
		}
		return ret;
	}

	public String toString() {
		String ret = "RobotSensor ";

		ret += getArrayAsString("dis", readings);

		return ret;
	}
}