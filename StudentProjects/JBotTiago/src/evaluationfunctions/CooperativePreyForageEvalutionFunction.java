package evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.CooperativePreyForageEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CooperativePreyForageEvalutionFunction  extends EvaluationFunction {

	int foodForaged = 0;
	
	public CooperativePreyForageEvalutionFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		CooperativePreyForageEnvironment environment = (CooperativePreyForageEnvironment) simulator.getEnvironment();
		
		for(Robot r : environment.getRobots()) {
			double closestPreyDistance = Double.MAX_VALUE;
			
			for(Prey p : environment.getPrey()) {
				double distance = p.getPosition().distanceTo(r.getPosition());
				if(distance < closestPreyDistance) {
					closestPreyDistance = distance;
				}
			}
			
//			System.out.print(closestPreyDistance + " " + simulator.getTime() + "\n");
			
			if(closestPreyDistance < Double.MAX_VALUE) {
				fitness += (environment.getForageRadius() - closestPreyDistance) / environment.getForageRadius() * .00005;
			}
			
//			if(closestPreyDistance < environment.preyDistance){
//				FakeActuator fakeActuator = (FakeActuator) r.getActuatorByType(FakeActuator.class);
//				if(fakeActuator.getValue() > 0.5)
//					fitness += 0.00005;
//			}
			
			if(r.isInvolvedInCollison())
				fitness -= 0.00005;
			
		}
		
		foodForaged = environment.getPreysCaught();
	}
	
	@Override
	public double getFitness() {
		return super.getFitness() + foodForaged	;
	}
	
}
