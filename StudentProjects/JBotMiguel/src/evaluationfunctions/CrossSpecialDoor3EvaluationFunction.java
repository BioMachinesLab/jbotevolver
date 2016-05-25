package evaluationfunctions;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsMultiEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CrossSpecialDoor3EvaluationFunction extends EvaluationFunction {
	
	private double steps = 0;
	private double bestMinX = 10000;
	private double penalty = 0;
	private double stayPutBonus = 0;
	private boolean doorClosed = false;

	public CrossSpecialDoor3EvaluationFunction(Arguments arguments) {
		super(arguments);
		doorClosed = arguments.getArgumentAsIntOrSetDefault("doorclosed",0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
		
		ArrayList<Robot> robots = simulator.getRobots();
		double maxX = -1000;
		double minX = 1000;
		boolean allInside = true;
		boolean crashed = false;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			double x = robots.get(i).getPosition().getX();
			
			if(x > maxX)
				maxX = x;
			if(x < minX)
				minX = x;
				
			if(x > 0.3 && x < 2) {
				allInside = false;
			}
			
			if(x < 0.3 && x > -0.3) {
				DifferentialDriveRobot r = (DifferentialDriveRobot)robots.get(i);
				if(r.getLeftWheelSpeed() == 0 && r.getRightWheelSpeed() == 0) {
					stayPutBonus+=0.001; 
				}
			}
			
			if(robots.get(i).isInvolvedInCollison()) {
				crashed = true;
				penalty-= 0.001;
			}
		}
		
		if(minX < bestMinX)
			bestMinX = minX;
		
		fitness = -bestMinX;
		
		boolean allCrossed = maxX <= -0.4;
		
		TwoRoomsMultiEnvironment env = (TwoRoomsMultiEnvironment)simulator.getEnvironment();
		
		if(!allInside && crashed) {
//			penalty-= 0.001;
		}
		
		if(!allInside && env.isFirstDoorClosed() && env.doorsOpen) {
			simulator.stopSimulation();
		}
		
//		if(allInside) {
//			fitness+=1;
//		}
		
		if(!doorClosed && !allCrossed && !env.doorsOpen) {
			simulator.stopSimulation();
		}
		
		if(bestMinX < -0.3) {
			fitness+=1;
		}
		
		if(allCrossed) {
			fitness+=1;
			fitness+=(steps-simulator.getTime())/steps;
			simulator.stopSimulation();
		}
		
		fitness+=penalty+stayPutBonus;
	}
}