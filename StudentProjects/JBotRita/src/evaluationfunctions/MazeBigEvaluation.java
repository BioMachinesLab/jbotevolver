
package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import environment.MazeBigEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class MazeBigEvaluation extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Double> preyInicialPosition = new HashMap<Prey, Double>();
    private double current=0.0;
    private double penalty=0.0;
    
	public MazeBigEvaluation(Arguments args) {
		super(args);	
	}

	
	//@Override
	public double getFitness() {
		if(numberOfFoodForaged>=1)
			return fitness + penalty;
		return fitness;
	}
	

	//@Override
	public void update(Simulator simulator) {	
		MazeBigEnvironment environment= ((MazeBigEnvironment)(simulator.getEnvironment()));
		ArrayList<Prey> preys= environment.getPrey();
		if(simulator.getTime()>0){
			if(environment.isToChange_PreyInitialDistance()){
				Prey prey= environment.preyWithNewDistace();
				preyInicialPosition.put(prey, prey.getPosition().distanceTo(nestPosition));
			}
			
			for(Prey p : environment.getPrey()) {  //Award for the prey is getting closer to the nest!
				double robotCaringPreyToTheNest=(1-(p.getPosition().distanceTo(nestPosition)/preyInicialPosition.get(p)))/(environment.getSteps()*1000);	
				if(robotCaringPreyToTheNest>0)
					current+=robotCaringPreyToTheNest;
			}
			
			
			for(Robot r : environment.getRobots()){ 
				
				if(r.isInvolvedInCollisonWall()){
						penalty-=0.0001;	
					
				}
				
				if(((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()){
					current+=1/(environment.getSteps()*10000);
				}
				
				if(preys.size()>0){ //Award for the robot being close to the closestPrey
				Prey closest_Prey=preys.get(0);
				Vector2d robot_position=r.getPosition();
					for(int i=1; i<preys.size(); i++){
						Prey current_prey=preys.get(i);
						if(robot_position.distanceTo(closest_Prey.getPosition())>robot_position.distanceTo(current_prey.getPosition()))
							closest_Prey=current_prey;
					}
					double robotGettingCloserToTheClosestPrey=(1-(robot_position.distanceTo(closest_Prey.getPosition())/preyInicialPosition.get(closest_Prey)))/(environment.getSteps()*10000);
					if(robotGettingCloserToTheClosestPrey>0)
						current+=robotGettingCloserToTheClosestPrey;		
				}
			}
			numberOfFoodForaged=environment.getNumberOfFoodSuccessfullyForaged();
			fitness=current+numberOfFoodForaged; 
			
		}else{  //tempo == 0
				for(Prey p :preys){
					preyInicialPosition.put(p, p.getPosition().distanceTo(nestPosition));
				}
		}
	}
}









