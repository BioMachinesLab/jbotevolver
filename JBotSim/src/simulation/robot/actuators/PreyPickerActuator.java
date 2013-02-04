package simulation.robot.actuators;

import java.util.Random;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PreyPickerActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	private enum PickerStatus {
		OFF, PICK, DROP
	}

	private double maxPickDistance;
	private PickerStatus status = PickerStatus.OFF;
	private Vector2d temp = new Vector2d();
	private boolean stopRobot;
	private Random random;
	private Prey preyCarried;
	private int numDrops;

	public PreyPickerActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		this.random = simulator.getRandom();
		this.maxPickDistance = arguments.getArgumentAsDoubleOrSetDefault("maxpickdistance", 0.1);

		this.stopRobot = !arguments.getFlagIsFalse("stoprobot");
	}

	public void pick() {
		status = PickerStatus.PICK;
	}

	public void drop() {
		status = PickerStatus.DROP;
	}

	public void reset() {
		status = PickerStatus.OFF;
	}

	@Override
	public void apply(Robot robot) {
		if (status != PickerStatus.OFF) {
			if (status == PickerStatus.PICK) {
				if ((random.nextFloat() > NOISESTDEV) && !isCarryingPrey()) {
					double bestLength = maxPickDistance;
					Prey bestPrey = null;
					ClosePhysicalObjects closePreys = robot.shape
							.getClosePrey();
					CloseObjectIterator iterator = closePreys.iterator();
					while (iterator.hasNext()) {
						Prey closePrey = (Prey) (iterator.next().getObject());
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
						pickUpPrey(robot, bestPrey);
					} else {
						status = PickerStatus.OFF;
					}

					// Stop robot
					if (stopRobot)
						robot.stop();
				}
			} else { // DROP
				if (isCarryingPrey()) {
					Prey prey = dropPrey();
					double offset = robot.getRadius() + prey.getRadius();
					Vector2d newPosition = new Vector2d(
							robot.getPosition().getX() + offset
									* Math.cos(robot.getOrientation()), robot
									.getPosition().getY()
									+ offset
									* Math.sin(robot.getOrientation()));
					prey.teleportTo(newPosition);

					// Stop robot
					if (stopRobot)
						robot.stop();
				}
			}
		}
	}

	
	/**
	 * Determine if the robot is currently carrying a prey.
	 *
	 * @return true if a prey is currently carried, false otherwise.
	 */	
	public boolean isCarryingPrey() {
		return preyCarried != null;
	}

	/**
	 * Pickup a prey (no checks are made).
	 */
	public void pickUpPrey(Robot robot, Prey prey) {
		preyCarried = prey;
		prey.setCarrier(robot);
	}

	/**
	 * Drop a prey (no checks are made).
	 */
	public Prey dropPrey() {
		numDrops++;
		Prey prey = preyCarried;
		prey.setCarrier(null);
		preyCarried = null;
		return prey;
	}
	
	public int getNumDrops() {
		return numDrops;
	}

	
	public int getNumberOfPreysCarried() {
		return preyCarried != null ? 1 : 0; 
			
	}

	public boolean getCanCarryMorePreys() {
		return preyCarried == null;
	}

	
	@Override
	public String toString() {
		return "PreyPickerActuator [maxPickDistance=" + maxPickDistance
				+ ", status=" + status + "]";
	}

}
