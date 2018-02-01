package evaluationfunctions;

import java.util.ArrayList;

import simulation.util.Arguments;

public class FlockingReynaldsWithHalfComponentCohension extends FlockingReynalds {
	
	public FlockingReynaldsWithHalfComponentCohension(Arguments args) {
		super(args);
	}
	
	@Override
	public double getFitness() {
		return currentFitnessForAlignment/simulator.getTime() + (currentFitnessForCohesion/simulator.getTime())*0.5-numberCollisions/simulator.getTime()+bootstrapingComponentCloserToPrey ;
	}
	

}
