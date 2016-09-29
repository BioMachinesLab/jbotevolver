package evaluationfunctions;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class AggregationEvaluationFunction extends EvaluationFunction {

	public AggregationEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		
		double currentFitness = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			if(robots.get(i).isInvolvedInCollison())
				continue;
			
			for(int j = i+1 ; j < robots.size() ; j++) {
				double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
				
				if(distance < 1)
					currentFitness+= 1-distance;
			}
		}
		fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
	}
}