package evaluationfunctions;


import java.util.HashMap;

import environment.WallsEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class Walls2PreyEvaluation extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Vector2d> preyInitialPosition = new HashMap<Prey, Vector2d>();
    private double current=0.0;
    private Vector2d position_ofPrey_closeToNest;
    
	public Walls2PreyEvaluation(Arguments args) {
		super(args);	
	}


	//@Override
	public void update(Simulator simulator) {	
		WallsEnvironment environment= ((WallsEnvironment)(simulator.getEnvironment()));
		if(simulator.getTime()>0){
			if(environment.isToChange_PreyInitialDistance()){
				Prey prey= environment.preyWithNewDistace();
				preyInitialPosition.replace(prey, prey.getPosition());
			}
			
			for(Prey p : simulator.getEnvironment().getPrey()) {
				current+=(1-(p.getPosition().distanceTo(nestPosition)/preyInitialPosition.get(p).distanceTo(nestPosition)))/(environment.getSteps());
			}
			
			for(Robot r : environment.getRobots()){
				if(((JumpingRobot) r).getRightWheelSpeed()<0 || ((JumpingRobot) r).getLeftWheelSpeed()<0){
					current+=-0.05;	
				}
				
				
				if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()==false) {
					Prey preyClose= environment.getPrey().get(0);
					Vector2d robotPosition= r.getPosition();
					for(int i=1; i<environment.getPrey().size();i++){
						Prey p=environment.getPrey().get(i);
						if(robotPosition.distanceTo(preyClose.getPosition())>robotPosition.distanceTo(p.getPosition()))
							preyClose=p;
					}
					//System.out.println(robotPosition.distanceTo(preyClose.getPosition())/robotPosition.distanceTo(preyInitialPosition.get(preyClose)));
					current+=(1-(robotPosition.distanceTo(preyClose.getPosition())/nestPosition.distanceTo(preyInitialPosition.get(preyClose))))/(environment.getSteps()+environment.getRobots().size());
				}
				
			}
			numberOfFoodForaged=environment.getNumberOfFoodSuccessfullyForaged();
			fitness=current+numberOfFoodForaged; 
			
		}else{  //tempo == 0
			
				for(Prey p :environment.getPrey()){
					preyInitialPosition.put(p,p.getPosition());
				}
			
		}
	}
	
}