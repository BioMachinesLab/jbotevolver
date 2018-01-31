package actuator;


import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;


/**
 * Similar to IntensityPreyPickerActuator:
 * "Remove/take/eat a bit from a prey (according to a specified intensity)" 
 * but each robot has a limit of the amount it can eat
 * ex: can only eat two bites of the prey
 * 
 * @author Rita Ramos
 */


public class LimitIntensityPreyPickerActuator extends
		IntensityPreyPickerActuator {
	
	@ArgumentsAnnotation(name = "limitOfTaking", defaultValue = "1")
	protected double limitOfTaking;
	
	public LimitIntensityPreyPickerActuator(Simulator simulator,
			int id, Arguments args) {
		super(simulator, id, args);
		
		this.limitOfTaking = args.getArgumentAsDoubleOrSetDefault(
				"limitOfTaking", 1);
	}
	
	@Override
	public void apply(Robot robot,double timeDelta) {
			if(isToPick){
				if(taking<=limitOfTaking){
					checkIfPickingIsAllowed(robot);
				}
			}
	}
	
	/**
	 * If the robot wants to eat prey, remove the corresponding amount eaten from the prey
	 * and update the amount of bites it can still eat
	 */

	@Override
	public void pickUpPrey(Robot robot, IntensityPrey prey) {
			isPicking=true;
			prey.setIntensity(prey.getIntensity() - taking);
			limitOfTaking=limitOfTaking-taking;
	}
		
}