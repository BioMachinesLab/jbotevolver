package evolutionaryrobotics.evaluationfunctions;

import java.awt.Color;
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

	private double safetyDistance = 1;
	private double minDistanceOthers = Double.POSITIVE_INFINITY;
	private boolean killOnCollision = false;
	private boolean safetyFactorEnable = false;
	private boolean energyFactorEnable = false;
	private boolean orientationFactorEnable = false;
	private boolean distanceBootstrapFactorEnable = false;
	private double inTargetRadius=1.0;

	private Simulator simulator;
	private Arguments args;

	// Entities and their positions
	private List<Target> targets = new ArrayList<Target>();
	private HashMap<Target, Vector2d> targetsPositions = new HashMap<Target, Vector2d>();
	private List<AquaticDroneCI> robots = new ArrayList<AquaticDroneCI>();

	private HashMap<AquaticDroneCI, Double> spentEnergy = new HashMap<AquaticDroneCI, Double>();
	private HashMap<AquaticDroneCI, Double> timeInTarget = new HashMap<AquaticDroneCI, Double>();

	private double initialDistance = 10;

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
		inTargetRadius = args.getArgumentAsDoubleOrSetDefault("inTargetRadius", inTargetRadius);
	}

	@Override
	public void update(Simulator simulator) {
		if (!configured) {
			configure(simulator);
		}

		// Update the targets positions
		for (Target target : targets) {
			Vector2d newPosition = CoordinateUtilities.GPSToCartesian(target.getLatLon());
			targetsPositions.remove(target);
			targetsPositions.put(target, newPosition);
		}

		// Update the robots status and target occupancy
		for (AquaticDroneCI robot : robots) {
			if (robot instanceof AquaticDrone) {
				if (((AquaticDrone) robot).hasFault()) {
					((AquaticDrone) robot).setBodyColor(Color.MAGENTA);
				} else {
					if (isInsideTarget(robot)) {
						((AquaticDrone) robot).setBodyColor(Color.BLUE);
					} else {
						((AquaticDrone) robot).setBodyColor(Color.BLACK);
					}
				}
			}
		}

		// Calculate in target reward and orientation factors
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
		}

		if (simulator.getTime() > 0) {
			double temporaryFitness = 0;
			double distance = 0;
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
						robotOrientation += 360;

					if (orientationDifference < 0)
						orientationDifference += 360;

					robotOrientation %= 360;
					orientationDifference %= 360;

					if (orientationDifference > 180) {
						orientationDifference = -((180 - orientationDifference) + 180);
					}

					orientationReward += 1-(FastMath.abs(orientationDifference) / 180.0);
					timeInTarget.put(robot, timeInTarget.remove(robot) + 1);
				}

				double energyFactor = spentEnergy.remove(robot);
				energyFactor += 1 - (robot.getMotorSpeedsInPercentage());
				spentEnergy.put(robot, energyFactor);
			}

			double meanDistance = distance / robots.size();
			double normalizedMeanDistance = (initialDistance - meanDistance) / initialDistance;

			// Occupied targets fitness component
			temporaryFitness += ((double) occupiedTargets / (double) robots.size())
					/ simulator.getEnvironment().getSteps() * 10;

			// Distance bootstrap fitness component
			if (distanceBootstrapFactorEnable) {
				temporaryFitness += (normalizedMeanDistance / simulator.getEnvironment().getSteps());
			}

			// Relative orientation fitness component
			if (orientationFactorEnable) {
				temporaryFitness += ((orientationReward * 10 / robots.size())
						/ (simulator.getEnvironment().getSteps()));
			}

			// Safety distance to relatives fitness component
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
		}
	}

	private void configure(Simulator simulator) {
		this.simulator = simulator;

		if (simulator.getEnvironment() instanceof TargetEnvironment) {
			radiusOfObjectPositioning = ((TargetEnvironment) simulator.getEnvironment()).getRadiusOfObjPositioning();
		}

		initialDistance = radiusOfObjectPositioning;

		// Get all targets instances
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

		// Get all AquaticDroneCI instances and calculate initial distance
		for (Robot robot : simulator.getRobots()) {
			if (robot instanceof AquaticDroneCI) {
				// initialDistance +=
				// getDistanceToClosestTarget(((AquaticDroneCI) robot), false);
				robots.add((AquaticDroneCI) robot);
				timeInTarget.put((AquaticDroneCI) robot, 0.0);
				spentEnergy.put((AquaticDroneCI) robot, 0.0);
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

	// If ignoreOccupied it will not consider the targets that are occupied
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

	// If ignoreOccupied and there is no unoccupied targets, it will return 0
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
		return pos.distanceTo(CoordinateUtilities.GPSToCartesian(robot.getGPSLatLon())) <= inTargetRadius;
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
