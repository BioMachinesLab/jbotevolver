
package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

import environments_JumpingSumoIntensityPreys.JS_Environment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class JumpingSumoPenaltyEvaluationFunction extends JumpingSumoEvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
//	private HashMap<Prey, Double> preyInicialPosition = new HashMap<Prey, Double>();
	private double preyInicialPosition;
    private double current=0.0;
    private double penalty=0.0;
    
	public JumpingSumoPenaltyEvaluationFunction(Arguments args) {
		super(args);	
	}


	//@Override
	public void update(Simulator simulator) {	
	//	Consuming1Environment environment= ((Consuming1Environment)(simulator.getEnvironment()));
		JS_Environment environment= ((JS_Environment)(simulator.getEnvironment()));
		ArrayList<Prey> preys= environment.getPrey();
		Vector2d preyPosition=preys.get(0).getPosition();
		if(simulator.getTime()>0){
//			if(environment.isToChange_PreyInitialDistance()){
//				Prey prey= environment.preyWithNewDistace();
//				preyInicialPosition.put(prey, prey.getPosition().distanceTo(nestPosition));
//			}
			
			for(Robot r : environment.getRobots()){ 
				
//				if(r.isInvolvedInCollisonWall()){
//					current-=0.03;			
//				}
				
				if(((IntensityPreyCarriedSensor)r.getSensorByType(IntensityPreyCarriedSensor.class)).preyCarried()){
					current+=1/(environment.getSteps()*10000);
				}

				
					Vector2d robot_position=r.getPosition();
				
					double robotGettingCloserToTheClosestPrey=(1-(robot_position.distanceTo(preyPosition)/preyInicialPosition))/(environment.getSteps()*10000);
				
					if(robotGettingCloserToTheClosestPrey>0)
						current+=robotGettingCloserToTheClosestPrey;		
				
			}
			numberOfFoodForaged=environment.getNumberOfFoodSuccessfullyForaged();
			if(numberOfFoodForaged==1){
				fitness=current+1+ (1-simulator.getTime()/environment.getSteps());
			}
			
		}else{  //tempo == 0
				if(preys.size()>0)
					preyInicialPosition= preyPosition.distanceTo(nestPosition);
		}
	}
}










/*
 * 
package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import sensors.PreyIntensityCarriedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.Consuming1Environment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class Consuming1EvaluationFunction extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Double> preyInicialPosition = new HashMap<Prey, Double>();
    private double current=0.0;
    private double penalty=0.0;
    
	public Consuming1EvaluationFunction(Arguments args) {
		super(args);	
	}

	


	//@Override
	public void update(Simulator simulator) {	
		Consuming1Environment environment= ((Consuming1Environment)(simulator.getEnvironment()));
		
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


*/
 


