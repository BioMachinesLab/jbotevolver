package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import actuator.LimitIntensityPreyPickerActuator;

/**
 * Sensor that indicates if the robot is currently picking a prey or not.
 * (if the robot is currently using the LimitIntensityPreyCarriedSensor)
 * @author Rita Ramos
 */

public class LimitIntensityPreyCarriedSensor extends IntensityPreyCarriedSensor {
	public LimitIntensityPreyCarriedSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	@Override	
	public boolean preyCarried() {
		if(actuator == null)
			actuator = (LimitIntensityPreyPickerActuator) robot.getActuatorByType(LimitIntensityPreyPickerActuator.class);
		return actuator.isCarryingPrey();
	}
	

	@Override
	public String toString() {
		return "LimitIntensityPreyCarriedSensor ["+preyCarried()+"]";
	}
	
	
	
}