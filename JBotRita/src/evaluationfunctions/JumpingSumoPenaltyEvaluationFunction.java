
package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments_ForagingIntensityPreys.ForagingIntensityPreysEnvironment;
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
		ForagingIntensityPreysEnvironment environment= ((ForagingIntensityPreysEnvironment)(simulator.getEnvironment()));
		ArrayList<Prey> preys= environment.getPrey();
		Vector2d preyPosition=preys.get(0).getPosition();
		if(simulator.getTime()>0){

			for(Robot r : environment.getRobots()){ 
				
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



