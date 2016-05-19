package evaluationfunctions;

import java.util.ArrayList;

import environment.JumpingEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class JumpEvaluation extends EvaluationFunction{

	private Vector2d  nestPosition = new Vector2d(0, 0);
	private int numberOfJumps = 0;
	double robotPercentage;
	double preyPercentage;
	double robotDistance;
	double preyDistance;
	
	public JumpEvaluation(Arguments args) 	{
		super(args);
		this.robotPercentage = args.getArgumentAsDoubleOrSetDefault("robotpercentage", 1);
		this.preyPercentage = args.getArgumentAsDoubleOrSetDefault("preypercentage", 1);
		
		this.preyDistance = args.getArgumentAsDoubleOrSetDefault("preydistance", 1);
		this.robotDistance = args.getArgumentAsDoubleOrSetDefault("robotdistance", 1);
		
	}

	//@Override
	public void update(Simulator simulator) {
		
		int numberOfRobotsBeyondForbidenLimit       = 0;
		int numberOfRobotsBeyondForagingLimit       = 0;
		
		double forbidenArea = ((JumpingEnvironment) simulator.getEnvironment()).getForbiddenArea();
		double foragingArea =  ((JumpingEnvironment)(simulator.getEnvironment())).getForageRadius();	
		double currentFitness = 0;
		
		for(Robot r : simulator.getEnvironment().getRobots()){
//			numberOfJumps= ((JumpingRobot) r).getNumberOfJumps();
			double distanceToNest = r.getPosition().distanceTo(nestPosition);
			
			for(Prey p : simulator.getEnvironment().getPrey()) {
				double distance = r.getPosition().distanceTo(p.getPosition());
				if(distance < preyDistance)
					currentFitness+= ((preyDistance-distance)*0.9);
			}
			
			if(distanceToNest > forbidenArea){
				numberOfRobotsBeyondForbidenLimit++;
				
			} else 	if(distanceToNest > foragingArea){
				numberOfRobotsBeyondForagingLimit++;
			}
			
			
//	naoooooooooooooooooo  mexerrrr!!!		
//			if(r.isInvolvedInCollison())
				
			System.out.println(numberOfRobotsBeyondForbidenLimit + "forbiddenlimit");
			currentFitness += numberOfJumps*0.003 + numberOfRobotsBeyondForbidenLimit *-2 + numberOfRobotsBeyondForagingLimit * -0.09;
		}
		fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
		System.out.println(fitness);
		//Depois tenho de ter o numero de saltos certos	
	}
}
	
	

