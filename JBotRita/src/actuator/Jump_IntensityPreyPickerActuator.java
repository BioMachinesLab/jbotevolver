package actuator;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class Jump_IntensityPreyPickerActuator extends Actuator {
	@ArgumentsAnnotation(name = "taking", defaultValue = "1")
	protected double taking;
	
	@ArgumentsAnnotation(name = "pickdistance", defaultValue = "0.1")
	protected double pickDistance;

	protected boolean hasPrey = false;
	
	protected Vector2d temp = new Vector2d();
	
	protected IntensityPrey bestPrey = null;

	
	public Jump_IntensityPreyPickerActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.pickDistance = args.getArgumentAsDoubleOrSetDefault(
				"pickdistance", 0.1);
		this.taking = args.getArgumentAsDoubleOrSetDefault("taking", 1);
		
	}

	@Override
	public void apply(Robot robot,double timeDelta) {
			if (!hasPrey) {
				findBestPrey(robot);
				if (bestPrey != null) {

					if (robot instanceof JumpingRobot) {
						if (!((JumpingRobot) robot).ignoreWallCollisions()) {
							pickUpPrey(robot, bestPrey);
						}
					} else {
						pickUpPrey(robot, bestPrey);
					}
				}
			}
	}
	
	protected void findBestPrey(Robot robot){
		double bestLength = pickDistance;
		bestPrey = null;
		ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
		CloseObjectIterator iterator = closePreys.iterator();
		while (iterator.hasNext()) {
			IntensityPrey closePrey = (IntensityPrey) (iterator.next()
					.getObject());
			if (closePrey.isEnabled()) {
				temp.set(closePrey.getPosition());
				temp.sub(robot.getPosition());
				double length = temp.length() - robot.getRadius();
				if (length < bestLength) {
					bestPrey = closePrey;
					bestLength = length;
				}
			}
		}
	}

	public void pickUpPrey(Robot robot, IntensityPrey prey) {
		hasPrey = true;
		prey.setIntensity(prey.getIntensity() - taking);
	}

	public void dropPrey() {
		hasPrey = false;
	}

	public boolean isCarryingPrey() {
		return hasPrey;
	}
}