
package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import environment.Consuming2Environment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import sensors.PreyIntensityCarriedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class Consuming2EvaluationFunction extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Double> preyInicialPosition = new HashMap<Prey, Double>();
    private double current=0.0;
    private double penalty=0.0;
    
	public Consuming2EvaluationFunction(Arguments args) {
		super(args);	
	}

	


	//@Override
	public void update(Simulator simulator) {	
		Consuming2Environment environment= ((Consuming2Environment)(simulator.getEnvironment()));
		ArrayList<Prey> preys= environment.getPrey();
		if(simulator.getTime()>0){
			if(environment.isToChange_PreyInitialDistance()){
				Prey prey= environment.preyWithNewDistace();
				preyInicialPosition.put(prey, prey.getPosition().distanceTo(nestPosition));
			}
			
			for(Robot r : environment.getRobots()){ 
				
				if(r.isInvolvedInCollisonWall()){
					current-=0.01;			
				}
				
				if(((PreyIntensityCarriedSensor)r.getSensorByType(PreyIntensityCarriedSensor.class)).preyCarried()){
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


