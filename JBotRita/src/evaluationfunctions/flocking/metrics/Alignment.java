package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


/**
 * Reward for robots being aligned, ie. matching the orientation
 * (this fitness function is the order metric commonly used in flocking studies,
 * having a value of 1 is the swarm is aligned and a value of 0 when there is no alignment)
 * @author Rita Ramos
 */

public class Alignment extends EvaluationFunction {

	protected Simulator simulator;
	
	public Alignment(Arguments args) {
		super(args);	
	}
	
	@Override
	public double getFitness() {
		return fitness/simulator.getTime();
	}
	
	

	@Override
	public void update(Simulator simulator) {	
		this.simulator=simulator;
		ArrayList<Robot> robots = simulator
				.getEnvironment().getRobots();
		double cos=0;
		double sen=0;
		
		for(Robot r: robots){
			double angleOfRobot=r.getOrientation();
			cos+=Math.cos(angleOfRobot);  
			sen+=Math.sin(angleOfRobot);
		}
		
		currentFitness=Math.sqrt(cos*cos+ sen*sen)/robots.size();
		fitness+= currentFitness;

	}
	
	

}
