package simulation.robot.actuators;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class SimplePreyPickerActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	private double maxPickDistance;
	private boolean toPick = false;
	private Vector2d temp = new Vector2d();
	private int numStepsToPick;
	private int stepsToPick = 0;
	private SimRandom random;

	public SimplePreyPickerActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id);
		this.random = simulator.getRandom();
		this.maxPickDistance = arguments.getArgumentAsDoubleOrSetDefault("maxpickdistance", 0.1);
		this.numStepsToPick = arguments.getArgumentAsIntOrSetDefault("numStepsToPick", 1);
	}

	public void pick() {
		toPick = true;
	}

	public void drop() {
		toPick = false;
	}

	@Override
	public void apply(Robot robot) {
		if (--stepsToPick <= 0 && toPick) {
			if (random.nextFloat() > NOISESTDEV) {
				double bestLength = maxPickDistance;
				Prey bestPrey = null;
				ClosePhysicalObjects closePreys = robot.shape
						.getClosePrey();
				CloseObjectIterator iterator = closePreys.iterator();
				while (iterator.hasNext()) {
					Prey closePrey = (Prey) (iterator.next().getObject());
					if (closePrey.getHolder() == null && closePrey.isEnabled()) {
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
					robot.pickUpPrey(bestPrey);
					stepsToPick = numStepsToPick;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "SimplePreyPickerActuator [maxPickDistance=" + maxPickDistance
				+ ", toPick=" + toPick + "]";
	}
}
