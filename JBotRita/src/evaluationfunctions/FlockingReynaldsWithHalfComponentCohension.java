package evaluationfunctions;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class FlockingReynaldsWithHalfComponentCohension extends FlockingReynalds {
	
	public FlockingReynaldsWithHalfComponentCohension(Arguments args) {
		super(args);
	}
	
	@Override
	public double getFitness() {
		return currentFitnessForAlignment/simulator.getTime() + (currentFitnessForCohesion/simulator.getTime())*0.5-numberCollisions/simulator.getTime()+bootstrapingComponentCloserToPrey ;
	}
	

}
