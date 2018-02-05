package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


/**
 * Reward for robots being aligned, ie. matching the orientation
 * (this fitness function is the order metric commonly used in flocking studies,
 * having a value of 1 is the swarm is aligned and a value of 0 when there is no alignment)
 * @author Rita Ramos
 */

public class EffectiveTraveledDistance extends EvaluationFunction {
	protected Simulator simulator;
	protected ArrayList<Robot> robots = new ArrayList<Robot>();
	private HashMap<Robot, Vector2d> inicialDistanceToPrey_perRobot = new HashMap<Robot, Vector2d>();

	
	public EffectiveTraveledDistance(Arguments args) {
		super(args);	
	}
	
	@Override
	public double getFitness() {
		Vector2d preyPosition = simulator.getEnvironment().getPrey().get(0).getPosition();
		for(Robot r : robots){
			Vector2d xT_x0= new Vector2d(r.getPosition().x- inicialDistanceToPrey_perRobot.get(r).x,r.getPosition().y- inicialDistanceToPrey_perRobot.get(r).y) ;
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
				inicialDistanceToPrey_perRobot.put(r, r.getPosition());
			}
		}
	}
	

}
