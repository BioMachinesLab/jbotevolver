package evaluationfunctions;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.IntensityForagingRobotsEnviroments;

public class FlockForaging extends ForagingWithJumpingSumo {

	private double numberCollisions = 0.0;
	private ArrayList<Robot> robots = new ArrayList<Robot>();
	private Simulator simulator;
	private IntensityForagingRobotsEnviroments environment=null;

	public FlockForaging(Arguments args) {
		super(args);
	}

	public double getFitness() {
		if(environment.getNumberOfFoodSuccessfullyForaged()==robots.size()){ //all preys were eaten
			double max_PossibleNumberOfCollisions=simulator.getTime();
			double penalty_for_collision= -numberCollisions/max_PossibleNumberOfCollisions;
			return 2+ (1 - simulator.getTime() / environment.getSteps()) + penalty_for_collision ;  //[1,3]
		}else{ 
			return environment.getNumberOfFoodSuccessfullyForaged()/ (double) robots.size();  //[0,1]
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

}