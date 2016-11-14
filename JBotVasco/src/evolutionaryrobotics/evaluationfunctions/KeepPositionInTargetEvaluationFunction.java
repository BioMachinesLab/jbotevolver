package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import environment.target.TargetEnvironment;
import evolutionaryrobotics.FormationTaskMetricsData;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

@SuppressWarnings("unused")
public class KeepPositionInTargetEvaluationFunction extends EvaluationFunction {
	private static final long serialVersionUID = 6856900415337660057L;

	// Parameters
	private boolean configured = false;
	private double radiusOfObjectPositioning = 20;
	private double safetyFactorValue = 0.5;
	private double bootstrapInitialDistance = 10;
	private double safetyDistance = 1;
	private double minDistanceOthers = Double.POSITIVE_INFINITY;
	private boolean killOnCollision = false;
	private boolean safetyFactorEnable = false;
	private boolean energyFactorEnable = false;
	private boolean orientationFactorEnable = false;
	private boolean distanceBootstrapFactorEnable = false;

	private Simulator simulator;
	private Arguments args;

	// Entities and their positions
	private List<Target> targets = new ArrayList<Target>();
	private HashMap<Target, Vector2d> targetsPositions = new HashMap<Target, Vector2d>();
	private List<AquaticDroneCI> robots = new ArrayList<AquaticDroneCI>();

	private HashMap<AquaticDroneCI, Double> spentEnergy = new HashMap<AquaticDroneCI, Double>();
	private HashMap<AquaticDroneCI, Double> timeInTarget = new HashMap<AquaticDroneCI, Double>();

	// Metrics variables
	private FormationTaskMetricsData formationTaskMetricsData = (FormationTaskMetricsData) metricsData;
	private HashMap<AquaticDroneCI, Target> targetOccupiedByRobot = new HashMap<AquaticDroneCI, Target>();
	private HashMap<AquaticDroneCI, Integer> targetOccupiedByRobotCount = new HashMap<AquaticDroneCI, Integer>();

	private Target targetOccupiedByFaultyRobot = null;
	private AquaticDroneCI faultyRobot = null;
	private double faultInjectionTime = -1;
	private double replaceTime = -1;
	private boolean inFault = false;

	private boolean firstTotalOccupationAchieved = false;

	public KeepPositionInTargetEvaluationFunction(Arguments args) {
		super(args);
		this.args = args;
		radiusOfObjectPositioning = args.getArgumentAsDoubleOrSetDefault("radiusOfObjectPositioning",
				radiusOfObjectPositioning);

		safetyFactorEnable = args.getArgumentAsIntOrSetDefault("safetyFactorEnable", 0) == 1;
		safetyDistance = args.getArgumentAsDoubleOrSetDefault("safetyDistance", safetyDistance);
		killOnCollision = args.getArgumentAsIntOrSetDefault("killOnCollision", 0) == 1;
		safetyFactorValue = args.getArgumentAsDoubleOrSetDefault("safetyFactorValue", safetyFactorValue);

		energyFactorEnable = args.getArgumentAsIntOrSetDefault("energyFactorEnable", 0) == 1;
		orientationFactorEnable = args.getArgumentAsIntOrSetDefault("orientationFactorEnable", 0) == 1;
		distanceBootstrapFactorEnable = args.getArgumentAsIntOrSetDefault("distanceBootstrapFactorEnable", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		if (!configured) {
			configure(simulator);
		}

		/*
		 * Update the targets positions
		 */
		for (Target target : targets) {
			Vector2d newPosition = CoordinateUtilities.GPSToCartesian(target.getLatLon());
			targetsPositions.remove(target);
			targetsPositions.put(target, newPosition);
		}

		/*
		 * Count occupied targets and get targets velocity vector angles
		 */
		int occupiedTargets = 0;
		double orientationReward = 0;
		HashMap<Target, Double> targetVelocityVectorAzimuth = new HashMap<Target, Double>();
		for (Target target : targets) {
			List<AquaticDroneCI> robotsInsideTarget = getRobotsInsideTarget(target);

			if (robotsInsideTarget.size() > 0) {
				occupiedTargets++;
			}

			if (target.getMotionData() != null) {
				double angle = target.getMotionData().getVelocityVector(simulator.getTime()).getAngle() * 180
						/ FastMath.PI;

				// Convert from JBotSim referential to geographic referential
				angle = 450 - angle;
				angle %= 360;

				targetVelocityVectorAzimuth.put(target, angle);
			}

			if (collectMetrics) {
				AquaticDroneCI drone = getClosestRobotToTarget(target);

				if (targetOccupiedByRobot.get(drone) == null) {
					targetOccupiedByRobot.put(drone, target);
					targetOccupiedByRobotCount.put(drone, targetOccupiedByRobotCount.get(drone) + 1);
				} else if (!targetOccupiedByRobot.get(drone).equals(target)) {
					targetOccupiedByRobot.remove(drone);
					targetOccupiedByRobot.put(drone, target);
					targetOccupiedByRobotCount.put(drone, targetOccupiedByRobotCount.get(drone) + 1);
				}

				// If in fault and the faulty robot was already replaced
				if (inFault && target.equals(targetOccupiedByFaultyRobot)
						&& !getClosestRobotToTarget(target).equals(faultyRobot)) {
					inFault = false;
					replaceTime = simulator.getTime();
				}
			}
		}

		if (simulator.getTime() > 0) {
			double temporaryFitness = 0;
			double distance = 0;

			/*
			 * Calculate distance bootstrap, orientation reward and energy
			 * Factor. Also check for robot fault conditions
			 */
			for (AquaticDroneCI robot : robots) {
				Target closest = getClosestTarget(robot, false);

				if (!isInsideTarget(robot)) {
					distance += (closest == null) ? 0
							: targetsPositions.get(closest)
									.distanceTo(CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon()));
				} else {
					double orientation = targetVelocityVectorAzimuth.get(closest);
					double robotOrientation = robot.getCompassOrientationInDegrees();
					double orientationDifference = robotOrientation - orientation;

					if (robotOrientation < 0)
						robotOrientation += 360.0;

					if (orientationDifference < 0)
						orientationDifference += 360.0;

					robotOrientation %= 360.0;
					orientationDifference %= 360.0;

					if (orientationDifference > 180.0) {
						orientationDifference = -((180.0 - orientationDifference) + 180.0);
					}

					orientationReward += 1 - (FastMath.abs(orientationDifference) / 180.0);
					timeInTarget.put(robot, timeInTarget.remove(robot) + 1);
				}

				// If a fault is detected, register fault beginning and actors
				if (collectMetrics && ((AquaticDrone) robot).hasFault()) {
					inFault = true;
					faultyRobot = robot;
					faultInjectionTime = simulator.getTime();

					if (isInsideTarget(robot)) {
						targetOccupiedByFaultyRobot = getClosestTarget(robot, true);
					}
				}

				double energyFactor = spentEnergy.remove(robot);
				energyFactor += 1 - (robot.getMotorSpeedsInPercentage());
				spentEnergy.put(robot, energyFactor);
			}

			double meanDistance = distance / robots.size();
			double normalizedMeanDistance = (bootstrapInitialDistance - meanDistance) / bootstrapInitialDistance;

			/*
			 * Calculate fitness components
			 */
			// Occupied targets fitness component
			temporaryFitness += ((double) occupiedTargets / robots.size()) / simulator.getEnvironment().getSteps() * 10;

			// Distance bootstrap fitness component
			if (distanceBootstrapFactorEnable) {
				temporaryFitness += (normalizedMeanDistance / simulator.getEnvironment().getSteps());
			}

			// Relative orientation fitness component
			if (orientationFactorEnable) {
				temporaryFitness += ((orientationReward * 10 / robots.size())
						/ (simulator.getEnvironment().getSteps()));
			}

			// Safety distance to relatives
			if (safetyFactorEnable) {
				for (int i = 0; i < robots.size(); i++) {
					if (robots.get(i) instanceof AquaticDrone) {
						AquaticDrone ri = (AquaticDrone) robots.get(i);

						for (int j = i + 1; j < robots.size(); j++) {
							if (robots.get(i) instanceof AquaticDrone) {
								AquaticDrone rj = (AquaticDrone) robots.get(i);

								minDistanceOthers = Math.min(minDistanceOthers,
										ri.getPosition().distanceTo(rj.getPosition()) - ri.getRadius()
												- rj.getRadius());
							}
						}

						if (killOnCollision && ri.isInvolvedInCollison()) {
							simulator.stopSimulation();
						}
					}
				}
				double safety = (1 - safetyFactorValue)
						+ (Math.max(0, Math.min(safetyDistance, minDistanceOthers)) / safetyDistance)
								* safetyFactorValue;
				temporaryFitness *= safety;

			}

			// Spent energy fitness component (only counts at the end of a run)
			if (energyFactorEnable && simulator.getTime() == simulator.getEnvironment().getSteps() - 1) {
				double energyComponent = 0;
				for (AquaticDroneCI robot : robots) {
					double spent = spentEnergy.get(robot);
					double time = timeInTarget.get(robot);

					energyComponent += spent * (time / simulator.getEnvironment().getSteps());
				}

				temporaryFitness += ((energyComponent / robots.size()) / (simulator.getEnvironment().getSteps() * 10));
			}

			// Sum this run fitness to total fitness
			fitness += temporaryFitness;

			/*
			 * Process metrics
			 */
			if (collectMetrics) {
				// Process on last time step
				if (simulator.getTime() == simulator.getEnvironment().getSteps() - 1) {
					// Time inside target and different occupied spots metrics
					// (Joined in the same if condition in benefit of
					// Performance)
					double min_time = Double.MAX_VALUE, min_occupation = Double.MAX_VALUE;
					double avg_time = 0, avg_occupation = 0;
					double max_time = Double.MIN_VALUE, max_occupation = Double.MAX_VALUE;

					for (AquaticDroneCI drone : robots) {
						double time = timeInTarget.get(drone);
						double occupation = targetOccupiedByRobotCount.get(drone);

						avg_time += time;
						avg_occupation += occupation;

						if (time < min_time) {
							min_time = time;
						}

						if (time > max_time) {
							max_time = time;
						}

						if (occupation < min_occupation) {
							min_occupation = occupation;
						}

						if (occupation > max_occupation) {
							max_occupation = occupation;
						}
					}

					avg_time /= robots.size();
					avg_occupation /= robots.size();

					formationTaskMetricsData.setTimeInside_min(min_time);
					formationTaskMetricsData.setTimeInside_avg(avg_time);
					formationTaskMetricsData.setTimeInside_max(max_time);

					formationTaskMetricsData.setNumberDiffSpotsOccupied_min(min_occupation);
					formationTaskMetricsData.setNumberDiffSpotsOccupied_avg(avg_occupation);
					formationTaskMetricsData.setNumberDiffSpotsOccupied_max(max_occupation);

				}

				// Reocupation time
				if (replaceTime != -1) {
					double replace = faultInjectionTime - replaceTime;
					formationTaskMetricsData.setReocupationTime_min(replace);
					formationTaskMetricsData.setReocupationTime_avg(replace);
					formationTaskMetricsData.setReocupationTime_max(replace);
				}

				// First total formation occupation metric
				if (occupiedTargets == targets.size()) {
					firstTotalOccupationAchieved = true;
					formationTaskMetricsData.setTimeFirstTotalOccup(simulator.getTime());
				}
			}
		}
	}

	private void configure(Simulator simulator) {
		this.simulator = simulator;

		/*
		 * Configure distance bootstrap
		 */
		if (simulator.getEnvironment() instanceof TargetEnvironment) {
			radiusOfObjectPositioning = ((TargetEnvironment) simulator.getEnvironment()).getRadiusOfObjPositioning();
		}
		bootstrapInitialDistance = radiusOfObjectPositioning;

		/*
		 * Get all targets instances
		 */
		for (Entity ent : ((AquaticDroneCI) simulator.getRobots().get(0)).getEntities()) {
			if (ent instanceof Formation) {
				Formation formation = (Formation) ent;

				targets.addAll(formation.getTargets());
				for (Target target : formation.getTargets()) {
					target.setOccupied(false);
					targetsPositions.put(target, CoordinateUtilities.GPSToCartesian(target.getLatLon()));
				}
			}
		}

		// Just in case...
		if (targets.isEmpty()) {
			throw new NullPointerException("No targets defined!");
		}

		/*
		 * Get all AquaticDroneCI instances and calculate initial distance
		 */
		for (Robot robot : simulator.getRobots()) {
			if (robot instanceof AquaticDroneCI) {
				// initialDistance +=
				// getDistanceToClosestTarget(((AquaticDroneCI) robot), false);
				robots.add((AquaticDroneCI) robot);
				timeInTarget.put((AquaticDroneCI) robot, 0.0);
				spentEnergy.put((AquaticDroneCI) robot, 0.0);
				targetOccupiedByRobotCount.put((AquaticDroneCI) robot, 0);
			}
		}

		// It is true if we only use AquaticDrones, which may not be the true...
		if (robots.size() != simulator.getRobots().size()) {
			throw new IllegalStateException("Different size!");
		}

		configured = true;
	}

	protected boolean safe(AquaticDroneCI robot) {
		if (robot instanceof AquaticDrone) {
			if (((AquaticDrone) robot).isInvolvedInCollison()) {
				return false;
			}

			if (safetyDistance > 0) {
				double min = Double.MAX_VALUE;

				for (AquaticDroneCI r : robots) {
					if (r instanceof AquaticDrone) {
						if (((AquaticDrone) r).getId() == ((AquaticDrone) robot).getId()) {
							// all robots with a lower ID are at safe distances,
							// so we can exit
							return true;
						}
					}

					double d = r.getGPSLatLon().distanceInMeters(robot.getGPSLatLon());
					if (d < safetyDistance) {
						return false;
					}

					min = Math.min(d, min);
				}
			}
		}

		return true;
	}

	private AquaticDroneCI getClosestRobotToTarget(Target target) {
		double minDistance = Double.MAX_VALUE;
		AquaticDroneCI closestRobot = null;

		Vector2d targetPosition = targetsPositions.get(target);
		for (AquaticDroneCI robot : robots) {
			Vector2d robotPosition = CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon());
			double distance = FastMath.abs(targetPosition.distanceTo(robotPosition));

			if (distance < minDistance) {
				closestRobot = robot;
				minDistance = distance;
			}
		}

		return closestRobot;
	}

	private List<AquaticDroneCI> getRobotsInsideTarget(Target target) {
		List<AquaticDroneCI> insideTarget = new ArrayList<AquaticDroneCI>();

		for (AquaticDroneCI robot : robots) {
			if (isInsideTarget(robot, target)) {
				insideTarget.add(robot);
			}
		}

		return insideTarget;
	}

	/*
	 * If ignoreOccupied it will not consider the targets that are occupied
	 */
	private Target getClosestTarget(AquaticDroneCI robot, boolean ignoreOccupied) {
		Target target = null;

		double minDistance = Double.MAX_VALUE;
		for (Target t : targets) {
			if (CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon())
					.distanceTo(targetsPositions.get(t)) < minDistance) {
				if (ignoreOccupied && t.isOccupied()
						&& !t.getOccupantID().equals(Integer.toString(((AquaticDrone) robot).getId()))) {
					continue;
				}

				minDistance = CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon())
						.distanceTo(targetsPositions.get(t));
				target = t;
			}
		}

		return target;
	}

	/*
	 * If ignoreOccupied and there is no unoccupied targets, it will return 0
	 */
	private double getDistanceToClosestTarget(AquaticDroneCI robot, boolean ignoreOccupied) {
		Target target = getClosestTarget(robot, ignoreOccupied);

		if (target == null) {
			return 0;
		} else {
			return targetsPositions.get(target).distanceTo(CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon()));
		}
	}

	private boolean isInsideTarget(AquaticDroneCI robot, Target target) {
		Vector2d pos = targetsPositions.get(target);
		return pos.distanceTo(CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon())) <= target.getRadius();
	}

	private boolean isInsideTarget(AquaticDroneCI robot) {
		for (Target target : targets) {
			if (isInsideTarget(robot, target)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public double getFitness() {
		return super.getFitness() + 10;
	}
}
