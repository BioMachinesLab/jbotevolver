package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import actuator.LimitIntensityPreyPickerActuator;

public class Jump_LimitIntensityPreyCarriedSensor extends Sensor {
	private LimitIntensityPreyPickerActuator actuator;

	public Jump_LimitIntensityPreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public boolean preyCarried() {
		if(actuator == null)
			actuator = (LimitIntensityPreyPickerActuator) robot.getActuatorByType(LimitIntensityPreyPickerActuator.class);
		return actuator.isCarryingPrey();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return preyCarried() ? 1 : 0;
	}

	@Override
	public String toString() {
		return "FireCarriedSensor ["+preyCarried()+"]";
	}
}