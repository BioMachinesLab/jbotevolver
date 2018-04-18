package actuator;



import simulation.Simulator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

public class TwoWheelsMinSpeedActuator extends TwoWheelActuator {


	public TwoWheelsMinSpeedActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
	}

	public void setLeftWheelSpeed(double value) {
        leftSpeed = ((value*.90)+.10) *maxSpeed;
    }
	
	public void setRightWheelSpeed(double value) {
		rightSpeed = ((value*.90)+.10) *maxSpeed;
    }

	
	@Override
	public String toString() {
		return "TwoWheelActuatorWithLimitSpeed [leftSpeed=" + leftSpeed + ", rightSpeed="
				+ rightSpeed + "]";
	}
	
}