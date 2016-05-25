package evaluationfunctions;

import java.util.HashMap;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class JumpWallsEvaluation extends EvaluationFunction {
	private Vector2d nestPosition = new Vector2d(0, 0);
	private double forbidenArea;
	private double foragingArea;
	
	private HashMap<Robot, Double> inicialDistanceToNest_perRobot = new HashMap<Robot, Double>();

	public JumpWallsEvaluation(Arguments args) {
		super(args);
	}
	
	public void update(Simulator simulator) { 
		
		double current=0.0;
		if(simulator.getTime()>0){
			for(Robot r : simulator.getEnvironment().getRobots()){
					current+=1-r.getPosition().distanceTo(nestPosition)/inicialDistanceToNest_perRobot.get(r);
					if(((JumpingRobot) r).getRightWheelSpeed()<0 || ((JumpingRobot) r).getLeftWheelSpeed()<0){
						current+=-0.05;	
					}	
			}
			fitness=current;
		}
		else{  //tempo == 0
			for(Robot r : simulator.getEnvironment().getRobots()){
				inicialDistanceToNest_perRobot.put(r, r.getPosition().distanceTo(nestPosition));
			}
			
		}		
}
}

// int numberOfRobotsBeyondForbidenLimit = 0;
// int numberOfRobotsBeyondForagingLimit = 0;
//
// double forbidenArea = ((JumpingWallsEnvironment)
// simulator.getEnvironment()).getForbiddenArea();
// double foragingArea =
// ((JumpingWallsEnvironment)(simulator.getEnvironment())).getForageRadius();
// double currentFitness = 0;
//
// for(Robot r : simulator.getEnvironment().getRobots()){
// numberOfJumps= ((JumpingRobot) r).getNumberOfJumps();
// numberOfJumpSucess= ((JumpingRobot) r).getNumberOfJumpsSucess();
// double distanceToNest = r.getPosition().distanceTo(nestPosition);
// if(distanceToNest > forbidenArea){
// numberOfRobotsBeyondForbidenLimit++;
// } else if(distanceToNest > foragingArea){
// numberOfRobotsBeyondForagingLimit++;
// }
//
// System.out.println(numberOfRobotsBeyondForbidenLimit + "forbiddenlimit");
//
// for(PhysicalObject wall : simulator.getEnvironment().getStaticObjects()) {
// double distanceToWall = r.getPosition().distanceTo(wall.getPosition());
//
// if(0.1>=distanceToWall && distanceToWall<=0.2){
// fitness += 0.2;
// }
// else {
// fitness += (1/(Math.abs(0.01-distanceToWall)))*.4;
// }
//
// if(distanceToWall < foragingArea){
// currentFitness*=2;
// }
//
// if(distanceToNest<=0.5){
// System.out.println("TAS NO NINHOOOOOOOO");
// }
// }
// currentFitness += numberOfJumpSucess*5 +numberOfJumps*0.04
// +numberOfRobotsBeyondForbidenLimit *-2 + numberOfRobotsBeyondForagingLimit *
// -1;
// }
// fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
// System.out.println(fitness);
// //Depois tenho de ter o numero de saltos certos
