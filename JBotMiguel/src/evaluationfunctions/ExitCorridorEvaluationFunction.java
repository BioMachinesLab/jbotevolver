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

public class ExitCorridorEvaluationFunction extends EvaluationFunction {
	
	private double steps = 0;
	private double penalty = 0;
	private double totalDistance = 0;
	private Vector2d target = new Vector2d(-0.45,0);
	private double penaltyMultiplier = 1;
	private boolean outside = false;
	
	public ExitCorridorEvaluationFunction(Arguments arguments) {
		super(arguments);
		totalDistance = new Vector2d(0,0).distanceTo(target);
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
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			DifferentialDriveRobot r = (DifferentialDriveRobot)robots.get(i);
			
			double x = r.getPosition().getX();
			
			leftX = x < leftX ? x : leftX;
			rightX = x > rightX ? x : rightX;
			
			if(r.isInvolvedInCollison()) {
				penalty-= penaltyMultiplier/robots.size()/steps;
			}
			
			double distance = r.getPosition().distanceTo(target);
			fitness+= (totalDistance-distance)/totalDistance / robots.size();
		}
		
		boolean outside = rightX < -0.45;
		
		if(outside) {
			simulator.stopSimulation();
			outside = true;
			fitness+= (steps-simulator.getTime())/steps;
		}
		
	}
	
	@Override
	public double getFitness() {
		return outside ? fitness + penalty : fitness;
	}
}