package evolutionaryrobotics.evaluationfunctions;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import evaluation.AvoidCollisionsFunction;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AroundTargetEvaluationFunction extends AvoidCollisionsFunction {
	private static final long serialVersionUID = 1741624091192380610L;
	private boolean configured = false;
	private boolean energyEfficiency = false;
	private boolean printEnergy = false;
	private boolean printDistance = false;
	private double targetRadius = 1;

	private Target target = null;
	private Vector2d targetPosition = null;

	private double initialDistance = 0;
	private double meanDistance = 0;

	public AroundTargetEvaluationFunction(Arguments args) {
		super(args);
		targetRadius = args.getArgumentAsDoubleOrSetDefault("targetRadius", targetRadius);

		energyEfficiency = args.getArgumentAsIntOrSetDefault("energyEfficiency", 0) == 1;
		printEnergy = args.getArgumentAsIntOrSetDefault("printEnergy", 0) == 1;
		printDistance = args.getArgumentAsIntOrSetDefault("printDistance", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		if (!configured) {
			for (Entity ent : ((AquaticDroneCI) simulator.getRobots().get(0)).getEntities()) {
				if (ent instanceof Target) {
					target = (Target) ent;
				}
			}

			if (target == null) {
				throw new NullPointerException("No target defined!");
			}

			targetPosition = CoordinateUtilities.GPSToCartesian(target.getLatLon());

			for (Robot robot : simulator.getRobots()) {
				Vector2d robotPosition = CoordinateUtilities.GPSToCartesian(((AquaticDroneCI) robot).getGPSLatLon());
				initialDistance += FastMath.abs(targetPosition.distanceTo(robotPosition));
			}

			initialDistance /= simulator.getRobots().size();
			configured = true;
		}

		double distance = 0;
		double energy = 0;

		for (Robot robot : simulator.getRobots()) {
			AquaticDroneCI aquaticRobot = (AquaticDroneCI) robot;

			Vector2d dronePosition = CoordinateUtilities.GPSToCartesian(aquaticRobot.getGPSLatLon());
			double distanceToTarget = FastMath.abs(targetPosition.distanceTo(dronePosition));
			distance += distanceToTarget;

			if (energyEfficiency) {
				if (distance < 0.3) {
					energy += (1 - (aquaticRobot.getMotorSpeedsInPercentage())
							/ (simulator.getEnvironment().getSteps() * simulator.getRobots().size()
									* ((distanceToTarget > 0) ? distanceToTarget : Double.MAX_VALUE)));
				}
			}
		}

		distance /= simulator.getRobots().size();
		meanDistance += (initialDistance - distance) / initialDistance;

		if (simulator.getTime() > 0){
			fitness = (meanDistance / simulator.getTime());
			fitness += (energy / simulator.getRobots().size() * simulator.getEnvironment().getSteps());
		}

		if (printDistance){
			System.out.printf("t= %f\t initial distance: %f\t distance: %f\t meanDistance: %f%n", simulator.getTime(),
					initialDistance, distance, meanDistance);
		}

		if (printEnergy){
			System.out.println("energy: " + energy);
		}

		super.update(simulator);
	}
}
