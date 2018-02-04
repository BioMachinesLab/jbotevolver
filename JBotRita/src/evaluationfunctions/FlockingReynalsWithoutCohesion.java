package evaluationfunctions;

import simulation.util.Arguments;


public class FlockingReynalsWithoutCohesion extends FlockingReynalds {
	
	public FlockingReynalsWithoutCohesion(Arguments args) {
		super(args);
	}
	
	public double getFitness() {
		return currentFitnessForAlignment/simulator.getTime() + -numberCollisions/simulator.getTime()+bootstrapingComponentCloserToPrey ;
	}
	
	
	

}