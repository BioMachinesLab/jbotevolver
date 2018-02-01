package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import actuator.JumpPreyPickerActuator;


/**
 * Sensor that indicates if the robot is currently picking a prey or not.
 * (if the robot is currently using the Jump_PreyPickerActuator)
 * @author Rita Ramos
 */


public class JumpPreyCarriedSensor extends PreyCarriedSensor{
	
	public JumpPreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	@Override
	public boolean preyCarried() {
		if(actuator == null)
			actuator = (JumpPreyPickerActuator) robot.getActuatorByType(JumpPreyPickerActuator.class);
		return actuator.isCarryingPrey();
	}
	
	@Override
	public String toString() {
		return "PreyCarriedJumpingSensor ["+preyCarried()+"]";
	}
}