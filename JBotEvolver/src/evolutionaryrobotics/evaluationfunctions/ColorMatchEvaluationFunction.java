package evolutionaryrobotics.evaluationfunctions;

import java.util.Iterator;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ColorMatchEvaluationFunction extends EvaluationFunction{

	public ColorMatchEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {			
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