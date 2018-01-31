package actuator;


import simulation.Simulator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

/**
 * RotationActuator is similar to TwoWheels, having the wheels the same value
 * (so that the robot can just rotate instead of moving forward or backwards)
 * @author Rita Ramos
 */

public class RotationActuator extends TwoWheelActuator {

	public RotationActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);		
	}

	public void setLeftWheelSpeed(double value) {
		leftSpeed = -((value - 0.5) * maxSpeed * 2.0);
	}

	public void setRightWheelSpeed(double value) {
		rightSpeed = ((value - 0.5) * maxSpeed * 2.0);
	}

}