package evaluationfunctions;

import java.util.HashMap;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
/**
 * Reward for getting closer to the nest
 * (save robot initial distance to nest and then reward as the robot distance to the nest decreases)
 * @author Rita Ramos
 */

public class ReturningToNest extends EvaluationFunction {
	private Vector2d nestPosition = new Vector2d(0, 0);

	
	private HashMap<Robot, Double> inicialDistanceToNest_perRobot = new HashMap<Robot, Double>();

	public ReturningToNest(Arguments args) {
		super(args);
	}
	
	@Override
	public void update(Simulator simulator) { 
		double current=0.0;
		if(simulator.getTime()>0){
			for(Robot r : simulator.getEnvironment().getRobots()){
					current+=1-r.getPosition().distanceTo(nestPosition)/inicialDistanceToNest_perRobot.get(r);	
			}
			fitness=current;
		}
		else{  //time == 0
			for(Robot r : simulator.getEnvironment().getRobots()){
				inicialDistanceToNest_perRobot.put(r, r.getPosition().distanceTo(nestPosition));
			}
		}		
	}
}