package evaluationfunctions;

import java.util.ArrayList;

import mathutils.Vector2d;
import roommaze.TwoRoomsEnvironment;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsMultiEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class EnterCorridorEvaluationFunction extends EvaluationFunction {
	
	private double steps = 0;
	private double bonus = 0;
	private double penalty = 0;
	private double totalDistance = 0;
	private Vector2d target = new Vector2d(0,0);
	private double penaltyMultiplier = 1;

	public EnterCorridorEvaluationFunction(Arguments arguments) {
		super(arguments);
		totalDistance = new Vector2d(1,0).distanceTo(target);
		penaltyMultiplier = arguments.getArgumentAsDoubleOrSetDefault("penalty",1);
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
			
		
		fitness = 0;
		
		ArrayList<Robot> robots = simulator.getRobots();
		TwoRoomsMultiEnvironment env = (TwoRoomsMultiEnvironment)simulator.getEnvironment();
		
		double leftX = Double.MAX_VALUE;
		double rightX = -Double.MAX_VALUE;
		
		double bonusAmount = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			DifferentialDriveRobot r = (DifferentialDriveRobot)robots.get(i);
			
			double x = r.getPosition().getX();
			
			leftX = x < leftX ? x : leftX;
			rightX = x > rightX ? x : rightX;
			
			if(x < 0.2 && x > -0.2) {
				if(r.getLeftWheelSpeed() == 0 && r.getRightWheelSpeed() == 0) {
					bonusAmount+= 1.0/robots.size()/steps; 
				}
			}
			
			if(r.isInvolvedInCollison()) {
				penalty-= penaltyMultiplier/robots.size()/steps;
			}
			
			double distance = r.getPosition().distanceTo(target);
			fitness+= (totalDistance-distance)/totalDistance / robots.size();
		}
		
		boolean allInside = rightX < 0.2 && leftX > -0.2;
		
		if(!allInside && env.isFirstDoorClosed() && env.doorsOpen) {
			simulator.stopSimulation();
		}
		
		if(allInside) {
			bonus+=bonusAmount;
		}
		
	}
	
	@Override
	public double getFitness() {
		return fitness + bonus + penalty;
	}
}