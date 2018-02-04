package evaluationfunctions.flocking;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ReynoldsFlocking extends EvaluationFunction {
	
	protected double currentFitnessForAlignment, currentFitnessForCohesion, bootstrapingComponentCloserToPrey;
	
	protected int numberCollisions;

	protected double cos,sen;
	
	protected double cohension = 0;
	protected int numberOfRobotsForAvarage=0;
	
	protected double sum_RobotsGettingCloserToThePrey;
	
	protected Simulator simulator;

	public ReynoldsFlocking(Arguments args) {
		super(args);
	}
	
	public double getFitness() {
		return currentFitnessForAlignment/simulator.getTime() + currentFitnessForCohesion/simulator.getTime()-numberCollisions/simulator.getTime()+bootstrapingComponentCloserToPrey ;
	}
	
	@Override
	public void update(Simulator simulator) {
		init();
		ArrayList<Robot> robots = simulator.getRobots();
		this.simulator=simulator;
		double widthOfEnvironemnt=simulator.getEnvironment().getWidth();
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			Robot robot=robots.get(i);
			
			movement(robot);					
			separation(robot);
			alignment(robot);
			cohesion( i,robots ,robot,  widthOfEnvironemnt);
		}
		computeSwarmFitnessOfEachRule(robots);
	}
	
	
	
	
	
	protected void init(){
		cos=0;
		sen=0;
		cohension = 0;
		numberOfRobotsForAvarage=0;
		sum_RobotsGettingCloserToThePrey=0.0;
	}
	
	protected void separation(Robot robot){ 
		if(robot.isInvolvedInCollison()){    
			numberCollisions++;
		}
	}
	
	protected void alignment(Robot robot){
		double angleOfRobot=robot.getOrientation();  
		cos+=Math.cos(angleOfRobot);  
		sen+=Math.sin(angleOfRobot);
	}
	
	protected void cohesion(int i, ArrayList<Robot> robots , Robot robot, double widthOfEnvironemnt){
		for(int j = i+1 ; j < robots.size() ; j++) {  
			double percentageOfDistance=1-( robot.getPosition().distanceTo(robots.get(j).getPosition()))/ widthOfEnvironemnt;
			if(percentageOfDistance < 0){
				percentageOfDistance=0;
			}
			cohension+=percentageOfDistance ;
			numberOfRobotsForAvarage++;
		}
	}
	
	protected void movement(Robot robot){
		Vector2d preyPosition = simulator.getEnvironment().getPrey().get(0).getPosition();
		Vector2d nest = new Vector2d(0, 0);
		
		double initialDistanceToPrey = preyPosition.distanceTo(nest);  //movement
		sum_RobotsGettingCloserToThePrey += (1 - (robot.getDistanceBetween(preyPosition) / initialDistanceToPrey));
	}
	
	
	protected void computeSwarmFitnessOfEachRule(ArrayList<Robot> robots ){
		currentFitnessForAlignment+=Math.sqrt(cos*cos+ sen*sen)/robots.size();
		currentFitnessForCohesion+=cohension/numberOfRobotsForAvarage;
		double avarage_RobotsGettingCloserToThePrey = sum_RobotsGettingCloserToThePrey/robots.size();
		
		if (avarage_RobotsGettingCloserToThePrey > bootstrapingComponentCloserToPrey)
			bootstrapingComponentCloserToPrey = avarage_RobotsGettingCloserToThePrey;
	}
	
	
}
