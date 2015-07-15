package evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import environment.PreyForageEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PreyForageEvalutionFunction  extends EvaluationFunction {

	int foodForaged = 0;
	private int multicationFactor = 2;
	
	public PreyForageEvalutionFunction(Arguments args) {
		super(args);
		multicationFactor = args.getArgumentAsIntOrSetDefault("multifactor", multicationFactor);
	}

	@Override
	public void update(Simulator simulator) {
		PreyForageEnvironment environment = (PreyForageEnvironment) simulator.getEnvironment();
		
		for(Robot r : environment.getRobots()) {
			PreyPickerActuator actuator = (PreyPickerActuator)r.getActuatorByType(PreyPickerActuator.class);
			
			double closestPreyDistance = Double.MAX_VALUE;
			for(Prey p : environment.getPrey()) {
				double distance = p.getPosition().distanceTo(r.getPosition());
				if(distance < closestPreyDistance) {
					closestPreyDistance = distance;
				}
			}
			
			if(closestPreyDistance < Double.MAX_VALUE && !actuator.isCarryingPrey()) {
				fitness += (environment.getForageRadius() - closestPreyDistance) / environment.getForageRadius() * .0001;
			}
			
			if(actuator.isCarryingPrey()){
				double robotDistanceToNest = r.getPosition().distanceTo(environment.getNest().getPosition());
				if(robotDistanceToNest < environment.getForbiddenArea())
					fitness += (environment.getForbiddenArea() - robotDistanceToNest) / environment.getForbiddenArea() * .001;
			}
		}
		
		foodForaged = environment.getNumberOfFoodForaged();
	}
	
	@Override
	public double getFitness() {
		return super.getFitness() + foodForaged*multicationFactor;
	}
	
}
