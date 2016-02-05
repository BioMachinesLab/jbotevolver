package sensors;

import actuators.MultipleWheelPreyPickerActuator;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class MultipleWheelPreyCarriedSensor extends PreyCarriedSensor{
	
	public MultipleWheelPreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public boolean preyCarried() {
		if(actuator == null) {
			actuator = (MultipleWheelPreyPickerActuator) robot.getActuatorByType(MultipleWheelPreyPickerActuator.class);
		}
		return actuator.isCarryingPrey();
	}
}