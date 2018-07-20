
package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.RoundForageIntensityEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**
 * Reward for eating the prey(s)
 * (give a reward for the number of bites have been eaten so far;
 * also in order to boostrap the evolution: it is also given a reward for the robots getting closer to the closest prey)
 * @author Rita Ramos
 */



public class ForagingIntensityPreys extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Double> preyInicialPosition = new HashMap<Prey, Double>();
    private double current=0.0;
    
	public ForagingIntensityPreys(Arguments args) {
		super(args);	
	}


	@Override
	public void update(Simulator simulator) {	
		RoundForageIntensityEnvironment environment= ((RoundForageIntensityEnvironment)(simulator.getEnvironment()));
		ArrayList<Prey> preys= environment.getPrey();
		if(simulator.getTime()>0){
			if(environment.isToChange_PreyInitialDistance()){
				Prey prey= environment.preyWithNewDistace();
				preyInicialPosition.put(prey, prey.getPosition().distanceTo(nestPosition));
			}
			
			for(Robot r : environment.getRobots()){ 
				
				if(((IntensityPreyCarriedSensor)r.getSensorByType(IntensityPreyCarriedSensor.class)).preyCarried()){
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
			
		}else{  //time == 0
			for(Prey p :preys){
				preyInicialPosition.put(p, p.getPosition().distanceTo(nestPosition));
			
			}
			
		}
	}
}








