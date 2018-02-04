package evaluationfunctions.flocking;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.IntensityForagingRobotsEnviroments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**
 * Reward for the swarm eating the food patch as fast as possible:
 * (If the robots did not eat all the food path:
 * 		- the fitness function has a value in [0, 1] depending on how many bites the robots have eaten. 
 * If the swarm is successful:
 * 		- its fitness will be in the interval [1,3], depending on how long the swarm take to eat all the food patch. 
 * 		So, the reward is:
 * 		+ 1 for eating all; 
 * 		a bonus of +1 so that the penalty of colliding (-1) does not make the result worse than eating ;
 * 		and +1 for getting as fast as possible
 * @author Rita Ramos
 */


public class ForagingFlock extends EvaluationFunction {

	protected double numberCollisions = 0.0;
	protected ArrayList<Robot> robots = new ArrayList<Robot>();
	protected Simulator simulator;
	protected IntensityForagingRobotsEnviroments environment=null;

	public ForagingFlock(Arguments args) {
		super(args);
	}
	
	@Override
	public double getFitness() {
		if (environment.getNumberOfFoodSuccessfullyForaged()==robots.size()) { //all preys were eaten
			double timeSpent=simulator.getTime();
			double max_PossibleNumberOfCollisions=timeSpent*robots.size();
			double penalty_for_collision= -numberCollisions/max_PossibleNumberOfCollisions;
			return 2 + (1 - timeSpent / environment.getSteps()) + penalty_for_collision; //[1,3]
		} else {
			return boostrapingComponent();
		}
	}
	

	@Override
	public void update(Simulator simulator) {
		this.simulator=simulator;
		environment = (IntensityForagingRobotsEnviroments) simulator
				.getEnvironment();
		robots = environment.getRobots();
		for(Robot r: robots){
			if (r.isInvolvedInCollison()) {
				numberCollisions++;
				break;
			}
		}		
	}
	
	protected double boostrapingComponent(){
		return environment.getNumberOfFoodSuccessfullyForaged()/ (double) robots.size();  //[0,1]
	}

}