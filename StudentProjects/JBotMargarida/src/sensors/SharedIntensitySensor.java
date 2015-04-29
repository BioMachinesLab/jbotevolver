package sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import environment.GarbageCollectorEnvironment;

public class SharedIntensitySensor extends ConeTypeSensor {

	private Simulator simulator;
	private LinkedList<SharedInformation> memory;
	private int slices;
	private double[] intensityReadings;
	@ArgumentsAnnotation(name = "intensityinput", defaultValue = "0")
	private boolean intensityInput;
	@ArgumentsAnnotation(name = "share", defaultValue = "1")
	private boolean share;
	@ArgumentsAnnotation(name = "memoryrange", defaultValue = "10")
	private double memoryRange;
	@ArgumentsAnnotation(name = "stepstoforget", defaultValue = "200")
	private int stepsToForget;

	public SharedIntensitySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
		memory = new LinkedList<SharedInformation>();
		slices = numberOfSensors;
		intensityReadings = new double[slices];
		intensityInput = args.getArgumentAsIntOrSetDefault("intensityinput", 0) == 1;
		share = args.getArgumentAsIntOrSetDefault("share", 1) == 1;
		memoryRange = args.getArgumentAsIntOrSetDefault("memoryrange", 10);
		stepsToForget = args.getArgumentAsIntOrSetDefault("stepstoforget", 200);
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {

//		System.out.print("robot id: " + robot.getId());
//		for (SharedInformation info : memory) {
//			System.out.print("[");
//			System.out.print(String.format("%.02f", info.getPosition().getX())
//					+ ", "
//					+ String.format("%.02f", info.getPosition().getY())
//					+ " Intensity "
//					+ info.getIntensity()
//					+ " | time seen "
//					+ info.getTimeStep()
//					+ " | time left "
//					+ ((info.getIntensity() * 200)
//							- (simulator.getTime() - info.getTimeStep())));
//			System.out.print("] ");
//		}
//		System.out.println(" ");

		LinkedList<SharedInformation> infoToRemove = new LinkedList<SharedInformation>();
		for (SharedInformation info : memory) {
			if ((info.getIntensity() * stepsToForget) - (simulator.getTime() - info.getTimeStep()) <= 0) {
				infoToRemove.add(info);
			}
		}

		for (SharedInformation info : infoToRemove) {
			memory.remove(info);
		}

		try {

			for (int j = 0; j < readings.length; j++) {
				readings[j] = 0.0;
				if (intensityInput)
					intensityReadings[j] = 0.0;
			}

			if (share) {
				if (closeObjects != null)
					closeObjects.update(time, teleported);

				CloseObjectIterator iterator = getCloseObjects().iterator();

				while (iterator.hasNext()) {

					PhysicalObjectDistance source = iterator.next();

					if (source.getObject().isInvisible())
						continue;
					if (source.getObject().isEnabled()) {
						calculateSourceContributions(source);
						iterator.updateCurrentDistance(this.geoCalc
								.getDistanceBetween(sensorPosition,
										source.getObject(), time));
					}
				}

				if (checkObstacles)
					checkObstacles(time, teleported);
			}
			 
			for (int j = 0; j < readings.length; j++) {
				if (openingAngle > 0.018) { // 1degree
					SharedInformation info = getClosestInfo(j, memory);
					if (info != null) {
						readings[j] = calculateDistanceValue(info.getPosition());
						// System.out.println(readings[j]);

						if (intensityInput)
							intensityReadings[j] = calculateIntensityValue(info);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for (int j = 0; j < readings.length; j++) {
			if (openingAngle > 0.018) { // 1degree
				calculateContributionToSensor(j, source);
			}
		}
	}

	@Override
	protected double calculateContributionToSensor(int i,
			PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);

		if ((sensorInfo.getDistance() < getCutOff())
				&& (sensorInfo.getAngle() < (openingAngle / 2.0))
				&& (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			Robot r = (Robot) source.getObject();

			SharedIntensitySensor sensor = (SharedIntensitySensor) r
					.getSensorByType(SharedIntensitySensor.class);

			for (SharedInformation info : memory) {
				sensor.addInformation(info);
			}

			return 0;
		} else {
			return 0;

		}
	}

	private SharedInformation getClosestInfo(int i,
			LinkedList<SharedInformation> memory) {
		SharedInformation closest = null;
		double distance = Double.MAX_VALUE;
		for (SharedInformation info : memory) {
			GeometricInfo sensorInfo = getSensorGeometricInfo(i,
					info.getPosition());
			if ((sensorInfo.getDistance() < memoryRange)
					&& (sensorInfo.getAngle() < (openingAngle / 2.0))
					&& (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

				if (distance > robot.getPosition().distanceTo(
						info.getPosition())) {
					distance = robot.getPosition().distanceTo(
							info.getPosition());
					closest = info;
				}

			}

		}
		return closest;
	}

	private double calculateDistanceValue(Vector2d source) {
		double dist = robot.getPosition().distanceTo(source);
		return (memoryRange - dist) / memoryRange;
	}

	private double calculateIntensityValue(SharedInformation info) {
		//TODO: invert this function
		return (GarbageCollectorEnvironment.MAX_PREY_INTENSITY - info
				.getIntensity()) / GarbageCollectorEnvironment.MAX_PREY_INTENSITY;
	}

	public void addInformation(SharedInformation information) {
		SharedInformation repeatedInfo = null;
		for (SharedInformation info : memory) {
			if (info.getPosition().equals(information.getPosition()))
				repeatedInfo = info;
		}
		if (repeatedInfo == null)
			memory.add(information);
		else if (repeatedInfo.getTimeStep() < information.getTimeStep()) {
			memory.remove(repeatedInfo);
			memory.add(information);
		}
	}

	public void clearInformation(SharedInformation information) {
		memory.remove(information);
	}

	public LinkedList<SharedInformation> getMemory() {
		return memory;
	}

	@Override
	public int getNumberOfSensors() {
		if (intensityInput)
			return super.getNumberOfSensors() * 2;
		else
			return super.getNumberOfSensors();
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		if (intensityInput) {
			if (sensorNumber >= slices) {
				return intensityReadings[sensorNumber - slices];
			}else
				return readings[sensorNumber];
		} else
			return readings[sensorNumber];
	}

}
