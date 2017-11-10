package evaluationfunctions;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class FlockingReynalds extends EvaluationFunction {
	
	protected int numberCollisions;
	protected double currentFitnessForAlignment;
	protected double currentFitnessForCohesion;
	protected Simulator simulator;
	protected double bootstrapingComponentCloserToPrey;

	public FlockingReynalds(Arguments args) {
		super(args);
	}
	
	public double getFitness() {
		return currentFitnessForAlignment/simulator.getTime() + currentFitnessForCohesion/simulator.getTime()-numberCollisions/simulator.getTime()+bootstrapingComponentCloserToPrey ;
	}
	

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		this.simulator=simulator;
		Vector2d nest = new Vector2d(0, 0);

		
		double cohension = 0;
		double widthOfEnvironemnt=simulator.getEnvironment().getWidth();
		int numberOfRobotsForAvarage=0;
		double cos=0;
		double sen=0;
		double sum_RobotsGettingCloserToThePrey=0.0;
		Vector2d preyPosition = simulator.getEnvironment().getPrey().get(0).getPosition();
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			Robot robot=robots.get(i);
			
			if(robot.isInvolvedInCollison()){    //Collision
				numberCollisions++;
			}
			
			double initialDistanceToPrey = preyPosition.distanceTo(nest);  //movement
			sum_RobotsGettingCloserToThePrey += (1 - (robot.getDistanceBetween(preyPosition) / initialDistanceToPrey));
		
		
			double angleOfRobot=robot.getOrientation();  //Alignment
			cos+=Math.cos(angleOfRobot);  
			sen+=Math.sin(angleOfRobot);
			
			
			for(int j = i+1 ; j < robots.size() ; j++) {  //Cohension
				
				double percentageOfDistance=1-( robot.getPosition().distanceTo(robots.get(j).getPosition()))/ widthOfEnvironemnt;
				
				if(percentageOfDistance < 0){
					percentageOfDistance=0;
				}
				cohension+=percentageOfDistance ;
				numberOfRobotsForAvarage++;
			}
		
		}
		currentFitnessForAlignment+=Math.sqrt(cos*cos+ sen*sen)/robots.size();
		currentFitnessForCohesion+=cohension/numberOfRobotsForAvarage;
		double avarage_RobotsGettingCloserToThePrey = sum_RobotsGettingCloserToThePrey/robots.size();
		
		if (avarage_RobotsGettingCloserToThePrey > bootstrapingComponentCloserToPrey)
			bootstrapingComponentCloserToPrey = avarage_RobotsGettingCloserToThePrey;
		
	}
}
