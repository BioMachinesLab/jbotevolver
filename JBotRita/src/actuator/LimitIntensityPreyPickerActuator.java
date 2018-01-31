package actuator;


import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

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

	@Override
	public void pickUpPrey(Robot robot, IntensityPrey prey) {
			isPicking=true;
			prey.setIntensity(prey.getIntensity() - taking);
			limitOfTaking=limitOfTaking-taking;
	}
	
	
}