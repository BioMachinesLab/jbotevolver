package evaluationfunctions.flocking;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;


/**Reynolds' flocking fitness function, giving a reward for separation, cohesion and alignment (plus movement)
 * This fitness function can be easily extend to adapt those 3 rules, being in this case:
 * 	alignment: [0,1] having a value of 1 when the swarm is all aligned, and 0 when is not aligned
 *  separation: penalty for collisions
 *  cohesion: [0,1] having a value of 1 when the swarm is closer as possible to one another (as agregation)
 *  movement: reward for getting closer to the prey, although the robots cannot see the prey 
 *  		(so the idea is just to give a reward for moving)
 * @author Rita Ramos
 */

public class ReynoldsFlocking extends EvaluationFunction {
	
	protected double fitnessForAlignment, fitnessForCohesion, fitnessForMovement;
	
	protected int numberCollisions;

	protected double cos,sen;
	
	protected double cohension = 0;
	protected int numberOfRobotsForAvarage=0;
	
	@ArgumentsAnnotation(name="cohensionDistance", defaultValue="1.0")	
	protected double cohensionDistance;
	
	protected double movementContribution;
	
	protected Simulator simulator;

	public ReynoldsFlocking(Arguments args) {
		super(args);
		cohensionDistance = args.getArgumentIsDefined("cohensionDistance") ? args
				.getArgumentAsInt("cohensionDistance") : 1.0;
	}
	
	public double getFitness() {
		return fitnessForAlignment/simulator.getTime() + fitnessForCohesion/simulator.getTime()-numberCollisions/simulator.getTime()+fitnessForMovement ;
	}
	
	@Override
	public void update(Simulator simulator) {
		init();
		ArrayList<Robot> robots = simulator.getRobots();
		this.simulator=simulator;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			Robot robot=robots.get(i);
			
			movement(robot);					
			separation(robot);
			alignment(robot);
			cohesion( i,robots ,robot);
		}
		computeSwarmFitnessOfEachRule(robots);
	}
	
	
	
	
	
	protected void init(){
		cos=0;
		sen=0;
		cohension = 0;
		numberOfRobotsForAvarage=0;
		movementContribution=0.0;
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
	
	protected void cohesion(int i, ArrayList<Robot> robots , Robot robot){
		for(int j = i+1 ; j < robots.size() ; j++) {  
			if(robot.getPosition().distanceTo(robots.get(j).getPosition())<=cohensionDistance){
				cohension+=1;
			}
			numberOfRobotsForAvarage++;
		}
	}
	
	protected void movement(Robot robot){ //robot getting farther away from the center (0,0)
		double percentageOfDistanceToCenter=robot.getPosition().distanceTo(new Vector2d(0,0))/simulator.getEnvironment().getWidth();;
		movementContribution+= percentageOfDistanceToCenter>1? 1:percentageOfDistanceToCenter;
	}
	
	
	protected void computeSwarmFitnessOfEachRule(ArrayList<Robot> robots ){
		fitnessForAlignment+=Math.sqrt(cos*cos+ sen*sen)/robots.size();
		fitnessForCohesion+=cohension/numberOfRobotsForAvarage;
		double avarage_MovementContribution = movementContribution/robots.size();
		
		if (avarage_MovementContribution > fitnessForMovement)
			fitnessForMovement = avarage_MovementContribution;
	}
	
	
}
