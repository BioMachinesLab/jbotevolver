package evolutionaryrobotics.evaluationfunctions;

import java.util.Iterator;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;

public class ColorMatchEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;

	public ColorMatchEvaluationFunction(Simulator simulator) {
		super(simulator);
	}

	//@Override
	public double getFitness() {
		return fitness;
	}

	//@Override
	public void update(double time) {			
		Iterator<Robot> i = simulator.getEnvironment().getRobots().iterator();
		Robot base = i.next();
		double   colorToMatch[]  = base.getBodyColorAsDoubles();
		Vector2d positionToMatch = base.getPosition();  
		
		double stepFitness = 0;
		while (i.hasNext()) {
			Robot check = i.next();
			double[] colorToCheck = check.getBodyColorAsDoubles();
			stepFitness += 1 - 
				Math.abs(colorToMatch[0] - colorToCheck[0]) - 
				Math.abs(colorToMatch[1] - colorToCheck[1]) -
				Math.abs(colorToMatch[2] - colorToCheck[2]);
				
			stepFitness += 1 - check.getPosition().distanceTo(positionToMatch);
		}
		
		fitness += stepFitness;
	}
}
