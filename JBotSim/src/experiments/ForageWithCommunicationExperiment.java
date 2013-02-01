package experiments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.GroupedPreyEnvironment;
import simulation.environment.BeeEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ForageWithCommunicationExperiment extends Experiment {

	public ForageWithCommunicationExperiment(Simulator simulator,
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotsArguments, Arguments controllersArguments) {
		super(simulator, experimentArguments, environmentArguments,
				robotsArguments, controllersArguments);

	}

	@Override
	protected void placeRobotsUsingPlacement() {
		if (robotArguments.getArgumentAsString("placement").equalsIgnoreCase(
				"nest")) {

			for (Robot r : robots) {

				boolean tooCloseToSomeOtherRobot = false;

				
				double maxRadius = 0;
				try{
					maxRadius = ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNestRadius();
				}catch (Exception e) {}
				
				try{
					maxRadius = ((BeeEnvironment)(simulator.getEnvironment())).getNestRadius();
				}catch (Exception e) {}
				

				do {
					tooCloseToSomeOtherRobot = false;
					double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
					double radius = simulator.getRandom().nextDouble() * maxRadius;
					double positionX = Math.cos(angle) * radius;
					double positionY = Math.sin(angle) * radius;

					r.setPosition(new Vector2d(positionX, positionY));
					Robot closestRobot = getRobotClosestTo(r);
					if (closestRobot != null) {
						if (r.getPosition().distanceTo(
								closestRobot.getPosition()) < 0.05)
							tooCloseToSomeOtherRobot = true;
					}
					maxRadius += .05;
				} while (tooCloseToSomeOtherRobot);
				r.teleportTo(r.getPosition());
			}
		} else {
			super.placeRobotsUsingPlacement();
		}
	}

}
