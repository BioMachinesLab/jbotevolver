package evaluationfunctions.flocking;

import java.util.ArrayList;

import mathutils.Vector2d;
import sensors.RobotSensorWithSources;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ReynoldsTruly extends EvaluationFunction {
	
	protected double fitnessForAlignment, fitnessForCohesion, fitnessForMovement;
	
	protected int numberCollisions;

	protected double cos,sen;
	
	protected double currentCohesion = 0;
	
	@ArgumentsAnnotation(name="cohensionDistance", defaultValue="0.25")	
	protected double cohensionDistance;
	
	protected double currentMovement;
	
	protected Simulator simulator;
	
	protected ArrayList<Robot> robots;

	public ReynoldsTruly(Arguments args) {
		super(args);
		cohensionDistance = args.getArgumentIsDefined("cohensionDistance") ? args
				.getArgumentAsDouble("cohensionDistance") : 0.25;
	}
	
	public double getFitness() {
		//System.out.println("alignment"+ (fitnessForAlignment/simulator.getTime()) );
		//System.out.println("cohesion"+ (fitnessForCohesion/simulator.getTime()) );
		//System.out.println("movemnt"+ (fitnessForMovement/simulator.getTime()) );

		return fitnessForAlignment/simulator.getTime() + fitnessForCohesion/simulator.getTime()-numberCollisions/simulator.getTime()+fitnessForMovement ;
	}
	
	@Override
	public void update(Simulator simulator) {
		this.simulator=simulator;
		init();
		for(int i = 0 ; i <  robots.size() ; i++) {
			Robot robot=robots.get(i);
			
			alignment(robot);
			cohesion( i,robot);
			separation(robot);

			movement(robot);					

		}
		
		computeSwarmFitnessOfEachRule();
	}
	

	protected void init(){
		cos=0;
		sen=0;
		currentCohesion = 0;
		currentMovement=0.0;
		robots = simulator.getRobots();
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
	
	protected void cohesion(int i, Robot robot){
		
		Vector2d robotPosition=robots.get(i).getPosition();
		PhysicalObject [] neighboursPerceived=(((RobotSensorWithSources)robots.get(i).getSensorByType(RobotSensorWithSources.class)).getSourcesPerceived());;

		double centerOfMassX=0, centerOfMassY=0,numberOfNeighboursPerceived=0;
		
		for(int j = 0 ; j < robots.size() ; j++) {  
			
			if(contains(neighboursPerceived,robots.get(j))){
				Vector2d neighbourPosition=robots.get(j).getPosition();
				centerOfMassX+=neighbourPosition.x;
				centerOfMassY+=neighbourPosition.y;
				numberOfNeighboursPerceived++;
			}
			
		}
		
		Vector2d centerOfMass=new Vector2d(centerOfMassX/numberOfNeighboursPerceived, centerOfMassY/numberOfNeighboursPerceived);
		double distanceToCenterMass=robotPosition.distanceTo(centerOfMass);
		if(numberOfNeighboursPerceived>=1){
			currentCohesion+=1-distanceToCenterMass/cohensionDistance;
		}		
	}
	
	protected void movement(Robot robot){ //robot getting farther away from the center (0,0)
		double percentageOfDistanceToCenter=robot.getPosition().distanceTo(new Vector2d(0,0))/simulator.getEnvironment().getWidth();
		currentMovement+= percentageOfDistanceToCenter>1? 1:percentageOfDistanceToCenter;
	}
	
	
	protected void computeSwarmFitnessOfEachRule(){
		computeFitnessForAlignment();
		computeFitnessForCohesion();
		//Separation is computed in the end [nºcollisions/timesteps]
		computeFitnessForMovement();
	}
	
	protected void computeFitnessForAlignment(){
		fitnessForAlignment+=Math.sqrt(cos*cos+ sen*sen)/ robots.size();
	}
	
	protected void computeFitnessForCohesion(){
		fitnessForCohesion+=currentCohesion/ robots.size();
	}
	
	protected void computeFitnessForMovement(){
		double avarage_MovementContribution = currentMovement/ robots.size();
		if (avarage_MovementContribution > fitnessForMovement)
			fitnessForMovement = avarage_MovementContribution;	
	}
	
	protected boolean contains(PhysicalObject [] neighboursPerceived, Robot neighbour){
		for (PhysicalObject neighbourPerceived: neighboursPerceived){
			if(neighbourPerceived!=null && neighbourPerceived.equals(neighbour)){
				return true;
			}
		}
		return false;
	}
	
	
}