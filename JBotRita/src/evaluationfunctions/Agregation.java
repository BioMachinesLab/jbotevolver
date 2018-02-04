package evaluationfunctions;

import java.util.ArrayList;
import simulation.robot.Robot;
import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


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
	