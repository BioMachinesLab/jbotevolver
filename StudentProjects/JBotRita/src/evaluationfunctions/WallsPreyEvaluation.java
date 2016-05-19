package evaluationfunctions;

import java.util.HashMap;

import environment.MazeDifficultEnvironment;
import environment.WallsEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class WallsPreyEvaluation extends EvaluationFunction{
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private int numberOfFoodForaged = 0;
	private HashMap<Prey, Double> preyInicialDistance = new HashMap<Prey, Double>();
    private double current=0.0;
    
	public WallsPreyEvaluation(Arguments args) {
		super(args);	
	}


	//@Override
	public void update(Simulator simulator) {	
		WallsEnvironment environment= ((WallsEnvironment)(simulator.getEnvironment()));
		
		if(simulator.getTime()>0){
			if(environment.isToChange_PreyInitialDistance()){
				Prey prey= environment.preyWithNewDistace();
				preyInicialDistance.replace(prey, prey.getPosition().distanceTo(nestPosition));
			}
			
			for(Prey p : environment.getPrey()) {
				current+=(1-(p.getPosition().distanceTo(nestPosition)/preyInicialDistance.get(p)))/(environment.getSteps());	
			}
			
			for(Robot r : environment.getRobots()){
				if(((DifferentialDriveRobot) r).getRightWheelSpeed()<0 || ((DifferentialDriveRobot) r).getLeftWheelSpeed()<0){
					current+=-0.05;	
				}
				if(r.getPosition().distanceTo(nestPosition)>2.2){
					current-=0.0003;
				}
			}
			numberOfFoodForaged=environment.getNumberOfFoodSuccessfullyForaged();
			fitness=current+numberOfFoodForaged; 
			
		}else{  //tempo == 0
				for(Prey p :environment.getPrey()){
					preyInicialDistance.put(p, p.getPosition().distanceTo(nestPosition));
			}
		}
	}
	
}

////@Override
//	public void update(Simulator simulator) {			
//		double current=0.0;
//		double numberOfRobotsWithPrey=0.0;
//		if(simulator.getTime()>0){
//			for(Robot r : simulator.getEnvironment().getRobots()){
////				if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()) {
////					numberOfRobotsWithPrey++;
////					current+=numberOfRobotsWithPrey;
////				}j
//
//				for(Prey p : simulator.getEnvironment().getPrey()) {
////					current+=(1- r.getPosition().distanceTo(p.getPosition()))/2;
//					current+=1-(p.getPosition().distanceTo(nestPosition)/preyInicialDistance.get(p));		
//				}
//			}
//			numberOfFoodForaged=((WallsEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged();
//			fitness=current+numberOfFoodForaged*3;
//		
//	
//		}else{  //tempo == 0
//			for(Prey p : simulator.getEnvironment().getPrey()){
//				preyInicialDistance.put(p, p.getPosition().distanceTo(nestPosition));
//			}
//			
//		}		
//	}