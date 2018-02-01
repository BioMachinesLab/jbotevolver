package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import actuator.Jump_PreyPickerActuator;


/**
 * Sensor that indicates if the robot is currently picking a prey or not.
 * (if the robot is currently using the Jump_PreyPickerActuator)
 * @author Rita Ramos
 */


public class Jump_PreyCarriedSensor extends PreyCarriedSensor{
	
	public Jump_PreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	@Override
	public boolean preyCarried() {
		if(actuator == null)
			actuator = (Jump_PreyPickerActuator) robot.getActuatorByType(Jump_PreyPickerActuator.class);
		return actuator.isCarryingPrey();
	}
	
	@Override
	public String toString() {
		return "PreyCarriedJumpingSensor ["+preyCarried()+"]";
	}
}