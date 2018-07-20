package evaluationfunctions;

import java.util.ArrayList;
import simulation.robot.Robot;
import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**
 * Reward for getting closer from other robots
 * (for each robot, reward if it is getting closer from other robot, 
 * taking account the current distance to the robot and the max distance it can move away -width of environment)
 * @author Rita Ramos
 */

public class Agregation extends Dispersion {
	
	public Agregation(Arguments args) {
		super(args);
	}
	
	protected double getPercentageOfDistance(int i,int j, ArrayList<Robot> robots, double widthOfEnvironemnt ){		
		double percentageOfDistance=1-( robots.get(i).getPosition().distanceTo(robots.get(j).getPosition()))/ widthOfEnvironemnt;
		
		if(percentageOfDistance < 0){
			percentageOfDistance=0;
		}
		return percentageOfDistance;
	}
}
	