package simulation.robot.sensors;

import java.util.ArrayList;
import java.util.Random;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class RobotSensor extends ConeTypeSensor {

	protected double r;

	public int slices;
	
	protected Random random;

	public RobotSensor(Simulator simulator, int id, Robot robot, Arguments args) {
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
			
			return (getRange() - sensorInfo.getDistance()) / getRange();
		}
		return 0.0;
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