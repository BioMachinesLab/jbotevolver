package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import actuator.Jump_PreyPickerActuator;

public class Jump_PreyCarriedSensor extends Sensor{
	
	private Jump_PreyPickerActuator actuator;

	public Jump_PreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public boolean preyCarried() {
	
		if(actuator == null)
			actuator = (Jump_PreyPickerActuator) robot.getActuatorByType(Jump_PreyPickerActuator.class);
		return actuator.isCarryingPrey();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return preyCarried() ? 1 : 0;
	}

	@Override
	public String toString() {
		return "PreyCarriedJumpingSensor ["+preyCarried()+"]";
	}
}