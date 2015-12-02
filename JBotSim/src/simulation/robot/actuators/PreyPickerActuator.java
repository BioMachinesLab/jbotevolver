package simulation.robot.actuators;

import java.util.Random;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class PreyPickerActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	public enum PickerStatus {
		OFF, PICK, DROP
	}

	@ArgumentsAnnotation(name="maxpickdistance", defaultValue="0.1")
	protected double maxPickDistance;
	private PickerStatus status = PickerStatus.OFF;
	private Vector2d temp = new Vector2d();
	@ArgumentsAnnotation(name="stoprobot", help="Set to 1 to force robot to stop to pick up something.", values={"0","1"})
	private boolean stopRobot;
	private Random random;
	private Prey preyCarried;
	private int numDrops;

	public PreyPickerActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		this.setRandom(simulator.getRandom());
		this.maxPickDistance = arguments.getArgumentAsDoubleOrSetDefault("maxpickdistance", 0.1);

		this.setStopRobot(!arguments.getFlagIsFalse("stoprobot"));
	}

	public double getMaxPickDistance() {
		return maxPickDistance;
	}
	
	public void pick() {
		setStatus(PickerStatus.PICK);
	}

	public void drop() {
		setStatus(PickerStatus.DROP);
	}

	public void reset() {
		setStatus(PickerStatus.OFF);
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
		if (getStatus() != PickerStatus.OFF) {
			if (getStatus() == PickerStatus.PICK) {
				if ((getRandom().nextFloat() > NOISESTDEV) && !isCarryingPrey()) {
					double bestLength = maxPickDistance;
					Prey bestPrey = null;
					ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
					CloseObjectIterator iterator = closePreys.iterator();
					while (iterator.hasNext()) {
						Prey closePrey = (Prey) (iterator.next().getObject());
						if (closePrey.isEnabled()) {
							getTemp().set(closePrey.getPosition());
							getTemp().sub(robot.getPosition());
							double length = getTemp().length() - robot.getRadius();
							if (length < bestLength) {
								bestPrey = closePrey;
								bestLength = length;
							}
						}
					}
					if (bestPrey != null) {
						pickUpPrey(robot, bestPrey);
					} else {
						setStatus(PickerStatus.OFF);
					}

					// Stop robot
					if (isStopRobot()) {
						if(robot instanceof DifferentialDriveRobot) {
							DifferentialDriveRobot ddr = (DifferentialDriveRobot)robot;
							ddr.setWheelSpeed(0,0);
						}
					}
				}
			} else { // DROP
				if (isCarryingPrey()) {
					Prey prey = dropPrey();
					double offset = robot.getRadius() + prey.getRadius();
					Vector2d newPosition = new Vector2d(
							robot.getPosition().getX() + offset
									* FastMath.cosQuick(robot.getOrientation()), robot
									.getPosition().getY()
									+ offset
									* FastMath.sinQuick(robot.getOrientation()));
					prey.teleportTo(newPosition);

					// Stop robot
					if (isStopRobot()) {
						if(robot instanceof DifferentialDriveRobot) {
							DifferentialDriveRobot ddr = (DifferentialDriveRobot)robot;
							ddr.setWheelSpeed(0,0);
						}
					}
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
	
	public PickerStatus getStatus() {
		return status;
	}
	
	@Override
	public String toString() {
		return "PreyPickerActuator [maxPickDistance=" + maxPickDistance
				+ ", status=" + getStatus() + "]";
	}

	public void setStatus(PickerStatus status) {
		this.status = status;
	}

	public Vector2d getTemp() {
		return temp;
	}

	public void setTemp(Vector2d temp) {
		this.temp = temp;
	}

	public boolean isStopRobot() {
		return stopRobot;
	}

	public void setStopRobot(boolean stopRobot) {
		this.stopRobot = stopRobot;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

}
