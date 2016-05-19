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

public class CrossSpecialDoor5EvaluationFunction extends EvaluationFunction {
	
	private double steps = 0;
	private double bonus = 0;
	private double penalty = 0;
	private double totalDistance = 0;
	private Vector2d target = new Vector2d(-1,0);
	private boolean doorClosed = false;
	private boolean kill = false;
	private double penaltyMultiplier = 1;
	private boolean time = true;
	private boolean ignoreDoors = false;

	public CrossSpecialDoor5EvaluationFunction(Arguments arguments) {
		super(arguments);
		totalDistance = new Vector2d(1,0).distanceTo(target);
		doorClosed = arguments.getArgumentAsIntOrSetDefault("doorclosed", 0) == 1;
		kill = arguments.getArgumentAsIntOrSetDefault("kill",0) == 1;
		penaltyMultiplier = arguments.getArgumentAsDoubleOrSetDefault("penalty",1);
		time = arguments.getArgumentAsIntOrSetDefault("time", 0) == 1;
		ignoreDoors = arguments.getArgumentAsIntOrSetDefault("ignoredoors", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
			
		
		fitness = 0;
		
		ArrayList<Robot> robots = simulator.getRobots();
		
		double leftX = Double.MAX_VALUE;
		double rightX = -Double.MAX_VALUE;
		
		double bonusAmount = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			DifferentialDriveRobot r = (DifferentialDriveRobot)robots.get(i);
			
			double x = r.getPosition().getX();
			
			leftX = x < leftX ? x : leftX;
			rightX = x > rightX ? x : rightX;
			
			if(x < 0.3 && x > -0.3) {
				if(r.getLeftWheelSpeed() == 0 && r.getRightWheelSpeed() == 0) {
					bonusAmount+= 1.0/robots.size()/steps; 
				}
			}
			if(r.isInvolvedInCollison()) {
				penalty-= penaltyMultiplier/robots.size()/steps;
				if(kill)
					simulator.stopSimulation();
			}
			
			if(r.getPosition().getX() <= -0.4) {
				fitness+= 1.0/robots.size();
			} else {
				double distance = r.getPosition().distanceTo(target);
				fitness+= (totalDistance-distance)/totalDistance / robots.size();
			}
		}
		
		boolean allInside = rightX < 0.3;
		boolean allCrossed = rightX < -0.4;
		
		if(!allInside && bonusAmount > 0)
			bonus+=bonusAmount;
		
		if(!ignoreDoors) {
			
			TwoRoomsMultiEnvironment env = (TwoRoomsMultiEnvironment)simulator.getEnvironment();
		
			if(!allInside && env.isFirstDoorClosed() && env.doorsOpen) {
				simulator.stopSimulation();
			}
			
			if(!doorClosed && !allCrossed && !env.doorsOpen) {
				simulator.stopSimulation();
			}
		
		}
		
		if(allCrossed) {
			simulator.stopSimulation();
			if(time)
				fitness+= (steps-simulator.getTime())/steps;
			else
				fitness = 2;
			penalty = Math.min(penalty, 1);
		}
	}
	
	@Override
	public double getFitness() {
		return fitness + bonus + penalty;
	}
}