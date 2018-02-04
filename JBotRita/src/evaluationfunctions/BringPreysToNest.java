package evaluationfunctions;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import sensors.JumpPreyCarriedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.WallsEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**
 * Reward for robots bringing preys to the nest
 * (for each robot, a reward is given if is getting closer to the closest prey; 
 * also a reward is given for preys getting closer to the nest ; ie. for robots taking preys to the nest)
 * @author Rita Ramos
 */

public class BringPreysToNest extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Vector2d> preyInicialPosition = new HashMap<Prey, Vector2d>();
    private double current=0.0;
    
	public BringPreysToNest(Arguments args) {
		super(args);	
	}

	//@Override
	public void update(Simulator simulator) {	
		WallsEnvironment environment= ((WallsEnvironment)(simulator.getEnvironment()));
		ArrayList<Prey> preys= environment.getPrey();
		if(simulator.getTime()>0){
			if(environment.isToChange_PreyInitialDistance()){
				Prey prey= environment.preyWithNewDistace();
				preyInicialPosition.put(prey, prey.getPosition());
			}
			
			for(Prey p : environment.getPrey()) {  //Award for bringing the prey closer to the nest!
				current+=(1-(p.getPosition().distanceTo(nestPosition)/preyInicialPosition.get(p).distanceTo(nestPosition)))/(environment.getSteps()*1000);	
			}
			
			for(Robot r : environment.getRobots()){
				
				//penalty for moving backwards
				if(((DifferentialDriveRobot) r).getRightWheelSpeed()<0 || ((DifferentialDriveRobot) r).getLeftWheelSpeed()<0){ 
					current+=-0.05;	
				}
				
				//award for robot getting closer to the closest prey
				if (((JumpPreyCarriedSensor)r.getSensorByType(JumpPreyCarriedSensor.class)).preyCarried()==false) {
				Prey closest_Prey=preys.get(0);
				Vector2d robot_position=r.getPosition();
					for(int i=0; i<preys.size(); i++){
						Prey current_prey=preys.get(i);
						if(robot_position.distanceTo(closest_Prey.getPosition())>robot_position.distanceTo(current_prey.getPosition()))
							closest_Prey=current_prey;
					}
					current+=(1-(robot_position.distanceTo(closest_Prey.getPosition())/preyInicialPosition.get(closest_Prey).distanceTo(nestPosition)))/(environment.getSteps()*10000);
				}
			}
			numberOfFoodForaged=environment.getNumberOfFoodSuccessfullyForaged();
			fitness=current+numberOfFoodForaged;
		}else{  //tempo == 0
				for(Prey p :preys){
					preyInicialPosition.put(p, p.getPosition());
			}
		}
	}
	
}