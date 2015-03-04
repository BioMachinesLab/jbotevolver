package actuators;

import java.util.Random;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.Prey;
import simulation.robot.BoatRobot;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class GarbagePickerActuator extends Actuator {

	public enum PickerStatus {
		UP, DOWN, LOWING;
	}

	@ArgumentsAnnotation(name = "maxpickdistance", defaultValue = "0.2")
	private double maxPickDistance;

	@ArgumentsAnnotation(name = "maxgarbage", defaultValue = "3")
	private double maxGarbageInNet;

	@ArgumentsAnnotation(name = "lowingtime", defaultValue = "20")
	private double lowingTime;

	private int lowing = 0;

	private PickerStatus status = PickerStatus.UP;
	private Vector2d temp = new Vector2d();

	private int numLeft;
	private int numRight;

	public GarbagePickerActuator(Simulator simulator, int id,
			Arguments arguments) {
		super(simulator, id, arguments);
		this.maxPickDistance = arguments.getArgumentAsDoubleOrSetDefault(
				"maxpickdistance", 0.1);
		this.maxGarbageInNet = arguments.getArgumentAsDoubleOrSetDefault(
				"maxgarbage", 3);
		this.lowingTime = arguments.getArgumentAsDoubleOrSetDefault(
				"lowingtime", 20);

	}

	public void up() {

		status = PickerStatus.UP;
	}

	public void down() {
		if (status != PickerStatus.DOWN) {

			status = PickerStatus.LOWING;

		}
	}

	@Override
	public void apply(Robot robot) {
		GeometricCalculator geo = new GeometricCalculator();
		if (status == PickerStatus.LOWING) {
			lowing++;
			if (lowing >= lowingTime) {
				((BoatRobot) robot)
						.setEnergy(((BoatRobot) robot).getEnergy() - 4);
				status = PickerStatus.DOWN;
				lowing = 0;
			}
		}

		if (status == PickerStatus.DOWN) {
			((BoatRobot) robot).setEnergy(((BoatRobot) robot).getEnergy() - 4);
			double bestLength = maxPickDistance;
			Prey bestPrey = null;
			ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
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
				double size = 0.1;

				// right picker
				double xj = robot.getPosition().getX()
						+ FastMath.cosQuick(robot.getOrientation() - 3.14 / 2)
						* (robot.getRadius() + size / 2);
				double yj = robot.getPosition().getY()
						+ FastMath.sinQuick(robot.getOrientation() - 3.14 / 2)
						* (robot.getRadius() + size / 2);

				GeometricInfo infoRight = geo.getGeometricInfoBetween(
						new Vector2d(xj, yj), robot.getOrientation(), bestPrey,
						0);
				// left picker
				double xi = robot.getPosition().getX()
						+ FastMath.cosQuick(3.14 / 2 + robot.getOrientation())
						* (robot.getRadius() + size / 2);
				double yi = robot.getPosition().getY()
						+ FastMath.sinQuick(3.14 / 2 + robot.getOrientation())
						* (robot.getRadius() + size / 2);

				GeometricInfo infoLeft = geo.getGeometricInfoBetween(
						new Vector2d(xi, yi), robot.getOrientation(), bestPrey,
						0);

				if (infoLeft.getAngle() > -0.5 && infoLeft.getAngle() < 0.5)
					pickUpPreyWithLeft(robot, bestPrey);
				else if (infoRight.getAngle() > -0.5
						&& infoRight.getAngle() < 0.5)
					pickUpPreyWithRight(robot, bestPrey);
			}
		} else
			((BoatRobot) robot)
					.setEnergy(((BoatRobot) robot).getEnergy() - 0.5);

	}

	public void pickUpPreyWithLeft(Robot robot, Prey prey) {
		if (numLeft < 3)
			numLeft++;
		prey.setCarrier(robot);
	}

	public void pickUpPreyWithRight(Robot robot, Prey prey) {
		if (numRight < 3)
			numRight++;
		prey.setCarrier(robot);
	}

	public int getNumberOfGarbageCarried() {
		return numLeft + numRight;

	}

	public boolean getCanCarryMoreGarbageLeft() {
		return numLeft < maxGarbageInNet;
	}

	public boolean getCanCarryMoreGarbageRoght() {
		return numRight < maxGarbageInNet;
	}

	public PickerStatus getStatus() {
		return status;
	}

	public int getNumLeft() {
		return numLeft;
	}

	public void setNumLeft(int numLeft) {
		this.numLeft = numLeft;
	}

	public int getNumRight() {
		return numRight;
	}

	public void setNumRight(int numRight) {
		this.numRight = numRight;
	}

	public double getMaxPickDistance() {
		return maxPickDistance;
	}

	public void setMaxPickDistance(double maxPickDistance) {
		this.maxPickDistance = maxPickDistance;
	}

	public boolean isDown() {
		return status == PickerStatus.DOWN;
	}

}
