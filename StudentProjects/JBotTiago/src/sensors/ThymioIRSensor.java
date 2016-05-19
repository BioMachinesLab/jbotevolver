package sensors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.checkers.AllowWallRobotChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 * Sensors that mimic those of the thymio.
 */

@SuppressWarnings("serial")
public class ThymioIRSensor extends ConeTypeSensor {
	private static double RANGE = 5;
	private static double REAL_RANGE = 0.18;
	private static double THYMIO_RADIUS = 0.08;
	private int[] chosenReferences;
	@ArgumentsAnnotation(name = "numberofrays", defaultValue = "7")
	private int numberOfRays = 7;
	@ArgumentsAnnotation(name = "noiseenabled", defaultValue = "1")
	private boolean noiseEnabled = true;
	private double[][] rayReadings;
	@ArgumentsAnnotation(name = "offsetnoise", defaultValue = "0")
	private double offsetNoise = 0;
	private double[] offsetNoises;

	@ArgumentsAnnotation(name = "senserobot", defaultValue = "0")
	private boolean senseRobot = false;

	static double sensorReferenceValues[][] = {
			{ 4478.69, 4500.36, 4472.57, 4449.23, 4431.84, 4329.66, 3935.85, 3530.38, 3160.98, 2813.91, 2610.8, 2404.66, 2241.13, 2025.82, 1833.36, 1697.69, 1488.73, 1197.21, 0 },// left sensor
			{ 4619.02, 4623.55, 4600.4, 4569.63, 4538.28, 4401.56, 3929.59, 3568.82, 3234.4, 2978.98, 2741.11, 2595.57, 2310.58, 2138.38, 1996.15, 1825.61, 1669.01, 1484.74, 1230.81 },// front left sensor
			{ 4418.23, 4477.46, 4461.83, 4443.7, 4421.46, 4329.54, 3871.43, 3529.38, 3208.01, 2900.72, 2706.74, 2525.3, 2335.89, 2122.15, 1989.35, 1814.01, 1608.27, 1411.69, 1174.6 },// front sensor
			{ 4439.73, 4465.92, 4461.2, 4451.33, 4412.95, 4336.57, 3901.95, 3509.57, 3161.27, 2838.57, 2837.85, 2446.69, 2315.65, 2165.91, 1958.04, 1774.66, 1587.54, 1410.51, 1144.58 },// front right sensors
			{ 4471.57, 4500.17, 4465.46, 4437.83, 4398.75, 4197.94, 3721.5, 3332.29, 2951.71, 2616.99, 2448.82, 2208.58, 2018.14, 1818.36, 1624.67, 1402.81, 0, 0, 0 },// right sensor
			{ 4730.47, 4759.56, 4795.1, 4746.2, 4706.71, 4589.99, 4302.42, 3867.25, 3397.83, 3075.8, 2876.28, 2719.94, 2465.85, 2309.16, 2153.25, 1959.57, 1821.54, 1377.47, 0 },// back left sensor
			{ 4436.35, 4452.13, 4433.87, 4401.68, 4353.98, 4001.71, 3419.55, 2978.96, 2570.81, 2344.68, 2140.85, 1890.11, 1808.42, 1628.99, 1441.68, 1236.41, 0, 0, 0 },// back right sensor
	};

	static double sensorReferenceStdDeviation[][] = {
			{ 2.07, 1.77, 2.33, 2.7, 2.19, 1.65, 2.04, 2.58, 3.95, 1.44, 2.26, 2.76, 3.16, 3.82, 3.01, 4.55, 2.64, 4.57, 0 },// left sensor
			{ 1.3, 2.16, 3.93, 2.52, 2.3, 2.88, 2.6, 2.27, 2.73, 2.42, 2.61, 3.02, 3.71, 3.12, 6.17, 5.52, 3.62, 4.59, 3.49 },// front left sensor
			{ 5.19, 2.69, 3.14, 4.08, 3.19, 3.8, 2.82, 2.95, 3.13, 2.74, 2.9, 2.72, 2.72, 4.37, 1.73, 2.64, 3.47, 4.14, 5.4 },// front sensor
			{ 4, 2.6, 2.87, 4.16, 3.74, 2.41, 2.82, 3.09, 2.65, 3.18, 2.48, 1.88, 2.84, 1.87, 3.32, 3.46, 3.1, 3.51, 4.73 },// front right sensors
			{ 4.26, 4.04, 3.08, 2.98, 2.63, 2.82, 2.78, 3.46, 1.57, 1.62, 3.13, 1.93, 4.46, 6.5, 2.58, 3.7, 0, 0, 0 },// right sensor
			{ 3.1, 6.46, 1.97, 10.14, 6.12, 8.18, 19.48, 3.8, 3.59, 10.32, 2.45, 3.2, 6.05, 16.33, 6.88, 4.16, 6.01, 6.75, 0 },// back left sensor
			{ 2.3, 1.64, 1.78, 2.7, 2.35, 4.31, 2.49, 2.52, 4.79, 3.89, 3.19, 2.48, 5.93, 2.93, 4.17, 5.64, 0, 0, 0 },// back right sensor
	};

	static double distances[] = { 0, 0.5, 1, 1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

	protected Random random;
	private Vector2d[][] cones;
	private Vector2d[] sensorPositions;
	private double fullOpeningAngle;
	private double minimumDistances[][];
	@ArgumentsAnnotation(name = "cutoffangle", defaultValue = "45")
	private double cutoffAngle;
	private double[] sensorsOrientations;

	@ArgumentsAnnotation(name = "fixedsensor", defaultValue = "0")
	private boolean fixedSensor;

	// Debug
	public Vector2d[][][] rayPositions;

	public ThymioIRSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.random = simulator.getRandom();
		numberOfSensors = 7;
		this.readings = new double[numberOfSensors];
		range = RANGE;
		cutOff = REAL_RANGE;

		senseRobot = args.getArgumentAsIntOrSetDefault("senserobot", 0) == 1;

		cutoffAngle = args.getArgumentAsDoubleOrSetDefault("cutoffangle", 45);
		fixedSensor = args.getArgumentAsIntOrSetDefault("fixedsensor", 0) == 1;
		noiseEnabled = args.getArgumentAsIntOrSetDefault("noiseenabled", 1) == 1;
		numberOfRays = args.getArgumentAsIntOrSetDefault("numberofrays", 7);
		offsetNoise = args.getArgumentAsDoubleOrSetDefault("offsetnoise", 0);
		offsetNoises = new double[numberOfSensors];

		for (int i = 0; i < offsetNoises.length; i++) {
			double rand = simulator.getRandom().nextDouble() * offsetNoise;
			boolean negative = simulator.getRandom().nextBoolean();

			if (negative)
				rand *= -1;
			offsetNoises[i] = rand;
		}

		if (numberOfRays % 2 == 0)
			numberOfRays++;

		rayReadings = new double[numberOfSensors][numberOfRays];

		chosenReferences = new int[numberOfSensors];

		if (fixedSensor) {
			int index = 0;
			for (int i = 0; i < numberOfSensors; i++) {
				if (args.getArgumentIsDefined("fixedsensornumber"))
					index = args.getArgumentAsInt("fixedsensornumber");
				else if (args.getArgumentIsDefined("references")) {
					index = i + args.getArgumentAsInt("references") * 4;
				} else if (args.getArgumentIsDefined("randomizedreferences")) {
					index = 4 * args.getArgumentAsInt("randomizedreferences")
							+ simulator.getRandom().nextInt(4);
				}
				chosenReferences[i] = index;
			}
		} else {
			for (int i = 0; i < numberOfSensors; i++) {
				int random = simulator.getRandom().nextInt(
						sensorReferenceValues.length);
				chosenReferences[i] = random;
			}
		}

		this.fullOpeningAngle = openingAngle;
		this.openingAngle = openingAngle / 2;

		initAngles();
		setAllowedObjectsChecker(new AllowWallRobotChecker(robot.getId()));

		sensorPositions = new Vector2d[numberOfSensors];
		cones = new Vector2d[numberOfSensors][numberOfRays];
		for (int i = 0; i < numberOfSensors; i++) {
			sensorPositions[i] = new Vector2d();
			for (int j = 0; j < numberOfRays; j++)
				cones[i][j] = new Vector2d();
		}

		minimumDistances = new double[numberOfSensors][numberOfRays];
	}

	private void initAngles() {
		this.angles = new double[numberOfSensors];
		this.sensorsOrientations = new double[numberOfSensors];

		angles[0] = Math.toRadians(37);
		angles[1] = Math.toRadians(17);
		angles[2] = Math.toRadians(0);
		angles[3] = Math.toRadians(343);
		angles[4] = Math.toRadians(323);
		angles[5] = Math.toRadians(159);
		angles[6] = Math.toRadians(201);
		
		sensorsOrientations[0] = Math.toRadians(37);
		sensorsOrientations[1] = Math.toRadians(17);
		sensorsOrientations[2] = Math.toRadians(0);
		sensorsOrientations[3] = Math.toRadians(343);
		sensorsOrientations[4] = Math.toRadians(323);
		sensorsOrientations[5] = Math.PI;
		sensorsOrientations[6] = Math.PI;
		
	}

	private void updateCones() {

		rayPositions = new Vector2d[numberOfSensors][numberOfRays][2];
		minimumDistances = new double[numberOfSensors][numberOfRays];

		for (int sensorNumber = 0; sensorNumber < numberOfSensors; sensorNumber++) {
			double orientation = angles[sensorNumber] + robot.getOrientation();

			sensorPositions[sensorNumber].set(FastMath.cosQuick(orientation) * THYMIO_RADIUS + robot.getPosition().getX(),
					FastMath.sinQuick(orientation) * THYMIO_RADIUS + robot.getPosition().getY());

			double alpha = (this.fullOpeningAngle) / (numberOfRays - 1);

			double sensorOrientation = sensorsOrientations[sensorNumber] + robot.getOrientation();
			for (int i = 0; i < numberOfRays; i++) {

				cones[sensorNumber][i].set(
						FastMath.cosQuick(sensorOrientation - openingAngle + alpha * i) * REAL_RANGE + sensorPositions[sensorNumber].getX(),
						FastMath.sinQuick(sensorOrientation - openingAngle + alpha * i) * REAL_RANGE + sensorPositions[sensorNumber].getY());
			}
		}
		
	}

	/**
	 * Calculates the distance between the robot and a given PhisicalObject if
	 * in range
	 * 
	 * @return distance or 0 if not in range
	 */
	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		double sensorValue = 0;

		if (source.getObject().getType() != PhysicalObjectType.ROBOT) {

			Wall w = (Wall) source.getObject();

			for (int i = 0; i < numberOfRays; i++) {
				Vector2d cone = cones[sensorNumber][i];

				rayPositions[sensorNumber][i][0] = sensorPositions[sensorNumber];
				if (rayPositions[sensorNumber][i][1] == null)
					rayPositions[sensorNumber][i][1] = cone;

				Vector2d intersection = null;
				intersection = w.intersectsWithLineSegment(sensorPositions[sensorNumber], cone,FastMath.toRadians(cutoffAngle));

				if (intersection != null) {
					double distance = intersection.distanceTo(sensorPositions[sensorNumber]);
					cone.angle(intersection);

					if (distance < REAL_RANGE) {
						sensorValue = distanceToSensor(distance, sensorNumber);

						sensorValue += sensorValue * offsetNoises[sensorNumber];

						if ((minimumDistances[sensorNumber][i] == 0 || distance < minimumDistances[sensorNumber][i]) && sensorValue > rayReadings[sensorNumber][i]) {
							rayPositions[sensorNumber][i][1] = intersection;
							rayReadings[sensorNumber][i] = FastMath.max(sensorValue, rayReadings[sensorNumber][i]);
						}
					}
				}

			}

		} else if (source.getObject().getType() == PhysicalObjectType.ROBOT && senseRobot) {

			GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source.getObject().getPosition());

			double distance = sensorInfo.getDistance();

			Robot robot = ((Robot) source.getObject());

			distance -= robot.getRadius();

			if (distance < REAL_RANGE && (sensorInfo.getAngle() < (openingAngle / 2)) && (sensorInfo.getAngle() > (-openingAngle / 2))) 
				sensorValue = distanceToSensor(distance, sensorNumber);

			for (int i = 0; i < numberOfRays; i++)
				rayReadings[sensorNumber][i] = FastMath.max(sensorValue, rayReadings[sensorNumber][i]);
		}
		
		return sensorValue;
		
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		updateCones();

		if (closeObjects != null) {
			closeObjects.update(time, teleported);
		}
		
		try {
			
			for (int i = 0; i < numberOfSensors; i++) {
				for (int j = 0; j < numberOfRays; j++) {
					rayReadings[i][j] = 0.0;
				}
				readings[i] = 0.0;
			}
			
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while (iterator.hasNext()) {
				PhysicalObjectDistance source = iterator.next();
				if (source.getObject().isEnabled()) {
					calculateSourceContributions(source);
				}
			}

			for (int i = 0; i < numberOfSensors; i++) {
				double copy[] = new double[numberOfRays];

				for (int ray = 0; ray < copy.length; ray++) {
					copy[ray] = rayReadings[i][ray];
				}

				double avg = 0;
				double maxRays = 4;

				for (int m = 0; m < maxRays; m++) {
					int currentMaxIndex = 0;
					for (int z = 1; z < copy.length; z++) {
						if (copy[z] > copy[currentMaxIndex]) {
							currentMaxIndex = z;
						}
					}
					avg += copy[currentMaxIndex] / maxRays;
					copy[currentMaxIndex] = 0;
				}

				readings[i] = avg;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for (int j = 0; j < numberOfSensors; j++) {
			calculateContributionToSensor(j, source);
		}
	}

	protected void oldCalculateSourceContributions(PhysicalObjectDistance source) {
		for (int j = 0; j < numberOfSensors; j++) {
			readings[j] = FastMath.max(
					calculateContributionToSensor(j, source), readings[j]);
		}
	}

	private double distanceToSensor(double distance, int sensorNumber) {

		double[] sensorReferences = sensorReferenceValues[chosenReferences[sensorNumber]];
		double[] stdDeviations = sensorReferenceStdDeviation[chosenReferences[sensorNumber]];

		distance *= 100;// 0.01 in the simulator is 1cm. We use the value 1 as 1cm

		if (distance < distances[distances.length - 1]) {

			int i;
			for (i = 0; i < distances.length; i++) {
				if (distance <= distances[i]) {
					if (i == 0)
						return sensorReferences[i];

					double distanceBefore = distances[i - 1];
					double distanceAfter = distances[i];

					double linearization = (distance - distanceBefore) / (distanceAfter - distanceBefore);

					double sensorReferenceAfter = sensorReferences[i];
					double sensorReferenceBefore = sensorReferences[i - 1];
					double currentSensorValue = (sensorReferenceBefore - sensorReferenceAfter) * (1 - linearization) + sensorReferenceAfter;

					double noise = random.nextGaussian() * (int) stdDeviations[i];

					if (!noiseEnabled)
						noise = 0;

					return currentSensorValue + noise;
				}
			}
		}
		return 0.0;
	}

	@Override
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber, Vector2d source) {
		double orientation = angles[sensorNumber] + robot.getOrientation();
		
		sensorPosition.set(FastMath.cosQuick(orientation) * robot.getRadius()
				+ robot.getPosition().getX(), FastMath.sinQuick(orientation)
				* robot.getRadius() + robot.getPosition().getY());

		double sensorOrientation = sensorsOrientations[sensorNumber] + robot.getOrientation();
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(sensorPosition, sensorOrientation, source, time);

		return sensorInfo;
	}

	@Override
	public double[] getSensorsOrientations() {
		return sensorsOrientations;
	}

	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "ThymioIRSensors [readings=" + Arrays.toString(readings) + "]";
	}
	
}