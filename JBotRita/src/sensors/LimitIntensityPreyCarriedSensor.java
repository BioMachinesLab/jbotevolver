package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import actuator.LimitIntensityPreyPickerActuator;

public class LimitIntensityPreyCarriedSensor extends Sensor {
	private LimitIntensityPreyPickerActuator actuator;
	private boolean hasPicked;

	
	public LimitIntensityPreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		
	}
	
	public boolean preyCarried() {
		if(actuator == null)
				actuator = (LimitIntensityPreyPickerActuator) robot.getActuatorByType(LimitIntensityPreyPickerActuator.class);
		if(actuator.isCarryingPrey()){
				hasPicked=true;
		}
		return hasPicked;
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