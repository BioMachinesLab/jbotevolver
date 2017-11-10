package evaluationfunctions;

import java.util.ArrayList;

import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.Simulator;
import simulation.util.Arguments;
import environments_ForagingIntensityPreys.ForagingIntensityPreysEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


public class Agregation extends EvaluationFunction {
	
	private int numberCollisions;

	public Agregation(Arguments args) {
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
				double percentageOfDistance=1-( robots.get(i).getPosition().distanceTo(robots.get(j).getPosition()))/ widthOfEnvironemnt;
				
				if(percentageOfDistance < 0){
					percentageOfDistance=0;
				}
				currentFitness+=percentageOfDistance ;
				numberOfRobotsForAvarage++;
			}
		
		}
		fitness= currentFitness/numberOfRobotsForAvarage -numberCollisions/simulator.getTime();
	}
}
	