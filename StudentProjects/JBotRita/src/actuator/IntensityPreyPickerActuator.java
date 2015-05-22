package actuator;


import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class IntensityPreyPickerActuator extends Actuator {
	@ArgumentsAnnotation(name = "taking", defaultValue = "1")
	private double taking;
	@ArgumentsAnnotation(name = "pickdistance", defaultValue = "0.1")
	private double pickDistance;
	private boolean hasPrey = false;
	private Vector2d temp = new Vector2d();

	public IntensityPreyPickerActuator(Simulator simulator, int id,
			Arguments args) {
		super(simulator, id, args);
		this.pickDistance = args.getArgumentAsDoubleOrSetDefault(
				"pickdistance", 0.1);
		this.taking = args.getArgumentAsDoubleOrSetDefault("taking", 1);

	}

	@Override
	public void apply(Robot robot) {

		if (!hasPrey) {
			double bestLength = pickDistance;
			IntensityPrey bestPrey = null;
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
			if (bestPrey != null) {
				if(robot instanceof JumpingRobot){
					if(!((JumpingRobot)robot).isJumping())
						pickUpPrey(robot, bestPrey);
				}else{
					pickUpPrey(robot, bestPrey);
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
