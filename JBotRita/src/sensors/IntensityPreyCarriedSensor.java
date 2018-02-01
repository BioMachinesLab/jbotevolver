package sensors;

import actuator.IntensityPreyPickerActuator;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;


/**
 * Sensor that indicates if the robot is currently picking a prey or not.
 * (if the robot is currently using the IntensityPreyPickerActuator)
 * @author Rita Ramos
 */

public class IntensityPreyCarriedSensor extends Sensor{
	
	protected IntensityPreyPickerActuator actuator;

	public IntensityPreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public boolean preyCarried() {
		if(actuator == null)
			actuator = (IntensityPreyPickerActuator) robot.getActuatorByType(IntensityPreyPickerActuator.class);
		return actuator.isCarryingPrey();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return preyCarried() ? 1 : 0;
	}

	@Override
	public String toString() {
		return "PreyCarriedSensor ["+preyCarried()+"]";
	}
}