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
import simulation.util.ArgumentsAnnotation;

public class RobotRGBColorSensor extends ConeTypeSensor {
	protected boolean detectRed;
	protected boolean detectGreen;
	protected boolean detectBlue;

	protected double r;

	@ArgumentsAnnotation(name = "mode", values={"rgb","r","g","b"})
	protected double[] redReadings;
	protected double[] greenReadings;
	protected double[] blueReadings;

	public int slices;
	
	protected Random random;

	public RobotRGBColorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.random = simulator.getRandom();
		String modeStr = (args.getArgumentIsDefined("mode")) ? args.getArgumentAsString("mode") : "r";
		
		this.slices = numberOfSensors;
		this.openingAngle = openingAngle / 2;
		this.r = (Math.PI / 2) / openingAngle;

		if (modeStr.contains("r") || modeStr.contains("R")) {
			this.detectRed = true;
		}

		if (modeStr.contains("g") || modeStr.contains("G")) {
			this.detectGreen = true;
		}

		if (modeStr.contains("b") || modeStr.contains("B")) {
			this.detectBlue = true;
		}

		if (detectRed)
			redReadings = new double[slices];

		if (detectGreen)
			greenReadings = new double[slices];

		if (detectBlue)
			blueReadings = new double[slices];
		
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		readings = new double[slices];
		
		if (detectRed) {
			for (int j = 0; j < slices; j++) {
				redReadings[j] = 0.0;
			}
		}

		if (detectGreen) {
			for (int j = 0; j < slices; j++) {
				greenReadings[j] = 0.0;
			}
		}

		if (detectBlue) {
			for (int j = 0; j < slices; j++) {
				blueReadings[j] = 0.0;
			}
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

		if (sensorNumber < slices) {
			return readings[sensorNumber];
		} else {
			if (sensorNumber < slices * 2) {
				if (detectRed) {
					return redReadings[sensorNumber - slices];
				} else if (detectGreen) {
					return greenReadings[sensorNumber - slices];
				} else {
					return blueReadings[sensorNumber - slices];
				}
			} else {
				if (sensorNumber < slices * 3) {
					if (detectRed && detectGreen) {
						return greenReadings[sensorNumber - slices * 2];
					}
				} 
			}
		}
		return blueReadings[sensorNumber % slices];
	}

	public double getDistance(int sensorNumber) {
		return readings[sensorNumber];
	}

	public double getRed(int sensorNumber) {
		return redReadings[sensorNumber];
	}

	public double getBlue(int sensorNumber) {
		return blueReadings[sensorNumber];
	}

	public double getGreen(int sensorNumber) {
		return greenReadings[sensorNumber];
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		Robot nextRobot = (Robot) source.getObject();
		for (int j = 0; j < numberOfSensors; j++) {
			double newReading = calculateContributionToSensor(j, source)
			* (1 + random.nextGaussian() * NOISESTDEV);
			if (newReading > readings[j]) {
				readings[j] = newReading;
				double[] bodyColor = nextRobot.getBodyColorAsDoubles();
				if (detectRed)
					redReadings[j] = bodyColor[Robot.REDINDEX];
				if (detectGreen)
					greenReadings[j] = bodyColor[Robot.GREENINDEX];
				if (detectBlue)
					blueReadings[j] = bodyColor[Robot.BLUEINDEX];
			}
		}
	}

	@Override
	public int getNumberOfSensors() {
		return slices
		+ slices
		* ((detectRed ? 1 : 0) + (detectGreen ? 1 : 0) + (detectBlue ? 1
				: 0));
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
		String ret = "RobotRGBColorSensor ";

		ret += getArrayAsString("dis", readings);
		if (detectRed)
			ret += " -- " + getArrayAsString("red", redReadings);

		if (detectGreen)
			ret += " -- " + getArrayAsString("green", greenReadings);

		if (detectBlue)
			ret += " -- " + getArrayAsString("blue", blueReadings);

		return ret;
	}

}
