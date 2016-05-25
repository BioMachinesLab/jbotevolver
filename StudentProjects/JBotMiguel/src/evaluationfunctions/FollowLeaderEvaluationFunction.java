package evaluationfunctions;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class FollowLeaderEvaluationFunction extends EvaluationFunction {

	public FollowLeaderEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		
		double currentFitness = 0;
		
		for(int i = 0 ; i <  robots.size() - 1 ; i++) {
			
			if(robots.get(i).isInvolvedInCollison())
				continue;
			
			double distance = robots.get(i).getPosition().distanceTo(robots.get(robots.size()-1).getPosition());
			
			if(distance < 1)
				currentFitness+= 1-distance;
		}
		fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
	}
}