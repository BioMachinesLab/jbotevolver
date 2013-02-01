package simulation.robot;

import java.util.LinkedList;

import mathutils.Vector2d;

import simulation.Simulator;
import simulation.physicalobjects.Prey;

/**
 * Representation of a circular, differential drive robot, including its
 * physical characteristics such as size and color, sensors and actuators and
 * the controller.
 * 
 * @author smo
 */
public class MultiPreyForagerRobot extends Robot {

	protected LinkedList<Prey> preysCarried = new LinkedList<Prey>();

	protected int preysCarriedLimit = Integer.MAX_VALUE;

	protected double preysCarriedSpeedReductionFactor = 0.8;

	private int numberOfPenalizationSteps = 0;

	private int penalizationDueToColision;

	private int safeTimeLeft = 0;

	private int amountOfSafeTime;

	public MultiPreyForagerRobot(Simulator simulator, String name, double x,
			double y, double orientation, double mass, double radius, double distanceWheels,
			String color, int preysCarriedLimit,
			double preysCarriedSpeedReductionFactor,
			int penalizationDueToColision, int amountOfSafeTime) {
		super(simulator, name, x, y, orientation, mass, radius, distanceWheels, color);
		this.preysCarriedLimit = preysCarriedLimit;
		this.preysCarriedSpeedReductionFactor = preysCarriedSpeedReductionFactor;
		this.penalizationDueToColision = penalizationDueToColision;
		this.amountOfSafeTime = amountOfSafeTime;
	}

	@Override
	public void pickUpPrey(Prey prey) {
		if (preysCarried.isEmpty()) {
			safeTimeLeft = amountOfSafeTime;
		}
		preysCarried.add(prey);
		prey.setCarrier(this);
		// prey.setEnabled(true);
	}

	/**
	 * Drop a prey (no checks are made).
	 */
	public LinkedList<Prey> dropPreys() {
		// numDrops++;
		LinkedList<Prey> preys = preysCarried;
		drop(preys);
		preysCarried = new LinkedList<Prey>();
		return preys;
	}

	@Override
	public boolean isCarryingPrey() {
		return !preysCarried.isEmpty();
	}

	private void drop(LinkedList<Prey> preys) {
		for (Prey prey : preys) {
			prey.setCarrier(null);
		}
	}

	public void dropPrey(Prey preyToRelease) {
		preysCarried.remove(preyToRelease);
		preyToRelease.setCarrier(null);
	}

	@Override
	public int getNumberOfPreysCarried() {
		return preysCarried.size();
	}

	@Override
	public boolean getCanCarryMorePreys() {
		return (numberOfPenalizationSteps <= 0) ? getNumberOfPreysCarried() < preysCarriedLimit
				: false;
	}

	public void setPreysCarriedLimit(int limit) {
		preysCarriedLimit = limit;
	}

	@Override
	public void setWheelSpeed(double left, double right) {
		numberOfPenalizationSteps--;
		safeTimeLeft--;
		double factor = 0;
		if (numberOfPenalizationSteps <= 0) {
			factor = Math.pow(preysCarriedSpeedReductionFactor,
					(double) preysCarried.size());
		}
		super.setWheelSpeed(left * factor, right * factor);
	}

	public int getPreysCarriedLimit() {
		return preysCarriedLimit;
	}

	public void updatePreyPosition() {
		for (Prey prey : preysCarried) {
			Vector2d newPosition = new Vector2d(getPosition().getX(),
					getPosition().getY());
			prey.setPosition(newPosition);
		}

	}

	public void stopRobotDueToColision() {
		numberOfPenalizationSteps = penalizationDueToColision;
		super.setWheelSpeed(0, 0);
	}

	@Override
	public boolean isInvolvedInCollison() {
		if (safeTimeLeft > 0) {
			return false;
		}
		return super.isInvolvedInCollison();
	}

}
