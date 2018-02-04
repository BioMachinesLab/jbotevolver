package evaluationfunctions;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**
 * Reward for moving away from other robots
 * (for each robot, reward if it is moving away from other robot, 
 * taking account the current distance to the robot and the max distance it can move away -width of environment)
 * @author Rita Ramos
 */

public class Dispersion extends EvaluationFunction {
	
	private int numberCollisions;

	public Dispersion(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		
		double currentFitness = 0;
		
		double widthOfEnvironemnt=simulator.getEnvironment().getWidth();

		int numberOfRobotsForAvarage=0;
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			if(robots.get(i).isInvolvedInCollison()){
				numberCollisions++;
			}
							
			for(int j = i+1 ; j < robots.size() ; j++) {				
				double percentageOfDistance=getPercentageOfDistance(i,j,robots,widthOfEnvironemnt);
				currentFitness+=percentageOfDistance ;
				numberOfRobotsForAvarage++;
			}
		}
		fitness= currentFitness/numberOfRobotsForAvarage -numberCollisions/simulator.getTime();
	}
	
	
	protected double getPercentageOfDistance(int i,int j, ArrayList<Robot> robots, double widthOfEnvironemnt ){
		return ( robots.get(i).getPosition().distanceTo(robots.get(j).getPosition()))/ widthOfEnvironemnt;
	}
}