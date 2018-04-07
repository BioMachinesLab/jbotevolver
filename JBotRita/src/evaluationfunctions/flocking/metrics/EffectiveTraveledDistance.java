package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


/**
 * Evaluate the distance the swarm traveled into the goal direction
 * D=(xT −x0)·f
 * where f is the position vector of the food patch (prey), 
 * and xT and x0 are the robots’ position vectors at time T and 0, respectively,
 * being then computed the dot product between the two vectors.
 * @author Rita Ramos
 */

public class EffectiveTraveledDistance extends EvaluationFunction {
	protected Simulator simulator;
	protected ArrayList<Robot> robots = new ArrayList<Robot>();
	private HashMap<Robot, Vector2d> inicialPositionperRobot = new HashMap<Robot, Vector2d>();

	
	public EffectiveTraveledDistance(Arguments args) {
		super(args);	
	}
	
	
	@Override
	public double getFitness() {
		fitness=0;
		Vector2d preyPosition = simulator.getEnvironment().getPrey().get(0).getPosition();
		for(Robot r : robots){

			Vector2d xT_x0= new Vector2d(r.getPosition().x- inicialPositionperRobot.get(r).x,r.getPosition().y- inicialPositionperRobot.get(r).y) ;
			fitness+=xT_x0.x*preyPosition.x + xT_x0.y*preyPosition.y;
		}
		return fitness/(double)robots.size();
	}
	
	

	@Override
	public void update(Simulator simulator) {
		if(simulator.getTime()==0){  //time == 0
			this.simulator=simulator;
			robots=simulator.getEnvironment().getRobots();
			for(Robot r : robots){
				inicialPositionperRobot.put(r, new Vector2d(r.getPosition()));
			}
		}
	}
	

}
