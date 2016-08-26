package evorbc.qualitymetrics;

import net.jafama.FastMath;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class DistanceQualityMetric extends EvaluationFunction{

	protected Vector2d position;
	protected double distanceTraveled = 0;
	
	public DistanceQualityMetric(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		
		if(position != null) {
			distanceTraveled+=distanceTo(position, r.getPosition());
		} else {
			distanceTraveled = length(r.getPosition());
		}
		
		position = new Vector2d(r.getPosition());
	}
	
	@Override
	public double getFitness() {
		//minDistance is the distance in a straight line
		double minDistance = length(position);
		
		//the edge case where the best thing is to stay put
		if(distanceTraveled == 0)
			return 1;
		
		return minDistance/distanceTraveled;
	}

	//Here we redefine functions that are in the Vector2d class because
	//they are slightly imprecise due to the use of Jafama 
	public double distanceTo(Vector2d a, Vector2d b) {
		double x = a.x - b.x;
		double y = a.y - b.y;		
		return FastMath.sqrt(x*x+y*y);
	}
	
	public final double length(Vector2d a)
	{
		return FastMath.sqrt(a.x*a.x + a.y*a.y);
	}

}
