package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class PreyCarriedSensor extends Sensor{

	public PreyCarriedSensor( Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public boolean preyCarried() {

		return ((PreyPickerActuator) robot.getActuatorByType("PreyPickerActuator")).isCarryingPrey();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		if (((PreyPickerActuator) robot.getActuatorByType("PreyPickerActuator")).isCarryingPrey())
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return "PreyCarriedSensor ["+((PreyPickerActuator) robot.getActuatorByType("PreyPickerActuator")).isCarryingPrey()+"]";
	}
}