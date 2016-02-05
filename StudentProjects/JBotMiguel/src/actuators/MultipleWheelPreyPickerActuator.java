package actuators;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import fourwheeledrobot.MultipleWheelAxesActuator;
import fourwheeledrobot.MultipleWheelRepertoireActuator;

public class MultipleWheelPreyPickerActuator extends PreyPickerActuator {
	
	protected double stepsStopped = 50;

	public MultipleWheelPreyPickerActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		stepsStopped = arguments.getArgumentAsDoubleOrSetDefault("stepsstopped", stepsStopped);
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
						stopRobot(robot,true);
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

//					// Stop robot
//					if (isStopRobot()) {
//						stopRobot(robot,true);
//					}
				}
			}
		}
	}
	
	protected void stopRobot(Robot r, boolean val) {
		
		
		Actuator act = (r.getActuatorByType(MultipleWheelRepertoireActuator.class));
		if(act != null) {
			MultipleWheelRepertoireActuator mwact = (MultipleWheelRepertoireActuator)act;
			mwact.stop(val ? stepsStopped : 0);
		} else {
			act = (r.getActuatorWithId(1));
			if(act != null) {
				MultipleWheelAxesActuator mwact = (MultipleWheelAxesActuator)act;
				mwact.stop(val ? stepsStopped : 0);
			}
		}
	}
}
