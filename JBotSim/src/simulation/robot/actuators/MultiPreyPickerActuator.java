package simulation.robot.actuators;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class MultiPreyPickerActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	private enum PickerStatus {
		OFF, PICK;
	}

	private double maxPickDistance;
	private PickerStatus status = PickerStatus.OFF;
	private Vector2d temp       = new Vector2d();
	private boolean stopRobot;
	private SimRandom random;

	public MultiPreyPickerActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id);
		this.maxPickDistance = args.getArgumentAsDoubleOrSetDefault("maxpickdistance", 0.1);
		this.stopRobot = args.getArgumentAsIntOrSetDefault("stoprobot", 1) == 1;
		this.random = simulator.getRandom();
	}

	public void pick() {
		status = PickerStatus.PICK;
	}

	public void off() {
		status = PickerStatus.OFF;
	}

	@Override
	public void apply(Robot robot) {
		if (status == PickerStatus.PICK && robot.getCanCarryMorePreys()) {
			Prey preyPickedUp = null;
			
			if ((random.nextFloat() > NOISESTDEV)) {
				ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
				CloseObjectIterator iterator = closePreys.iterator();
				boolean preyFound = false;
				while (iterator.hasNext() && !preyFound) {
					Prey closePrey = (Prey) (iterator.next().getObject());
					if (closePrey.isEnabled()) {
						temp.set(closePrey.getPosition());
						temp.sub(robot.getPosition());
						double length = temp.length() - robot.getRadius();
						if (length <= maxPickDistance) {
							preyFound    = true;
							preyPickedUp = closePrey; 
						}
					}
				}
				if (preyPickedUp != null) {
					robot.pickUpPrey(preyPickedUp);
				}

				// Stop robot
				if (stopRobot)
					robot.setWheelSpeed(0, 0);
			}
		}
	}

	@Override
	public String toString() {
		return "MultiPreyPickerActuator [maxPickDistance=" + maxPickDistance
				+ ", status=" + status + "]";
	}
}