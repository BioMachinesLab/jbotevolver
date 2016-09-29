package evaluationfunctions;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsMultiEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CrossSpecialDoorEvaluationFunction extends EvaluationFunction {
	
	private double steps = 0;
	private boolean punishCollision = true;
	private double penalty = 0;
	private boolean closeRobots = false;
	
	private double distanceBonus = 0;
	private double targetBonus = 0;
	private double collisionPenalty = 0;
	private boolean dieOnCollision = false;

	public CrossSpecialDoorEvaluationFunction(Arguments arguments) {
		super(arguments);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		dieOnCollision = arguments.getArgumentAsIntOrSetDefault("dieoncollision", 0) == 1;
		collisionPenalty = arguments.getArgumentAsDoubleOrSetDefault("collisionpenalty", 0.001);
		closeRobots = arguments.getArgumentAsIntOrSetDefault("closerobots", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();

		if(punishCollision){
			for(Robot r : simulator.getRobots()) {
				if(r.isInvolvedInCollison()) {
					penalty+= collisionPenalty/simulator.getRobots().size();
	//				simulator.stopSimulation();
				}
			}
		}
		
		double maxX = -Double.MAX_VALUE;
		boolean allInside = true;
		
		ArrayList<Robot> robots = simulator.getRobots();
		double numberOfRobots = robots.size();
		int number = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			double x = robots.get(i).getPosition().getX();
			if(x > maxX)
				maxX = x;
				
			if(closeRobots) {
				for(int j = i+1 ; j < robots.size() ; j++) {
					double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
					number++;
					if(distance < 1)
						distanceBonus+= (1-distance)/simulator.getEnvironment().getSteps();
				}
			}
			
			if(x > 0.3 && x < 2) {
				targetBonus+= (2-Math.abs(x))/2.0/numberOfRobots/steps;
				allInside = false;
				if(dieOnCollision && robots.get(i).isInvolvedInCollison())
					simulator.stopSimulation();
					
			} else if(x < 0.3 && x > - 0.3) 
				targetBonus+= 2/numberOfRobots/steps;
		}

		boolean allCrossed = maxX <= -0.4;
		
		fitness = targetBonus;
		
		TwoRoomsMultiEnvironment env = (TwoRoomsMultiEnvironment)simulator.getEnvironment();
		
		if(!allInside && env.isFirstDoorClosed() && env.doorsOpen) {
			simulator.stopSimulation();
		}
		if(!allCrossed && !env.doorsOpen) {
			simulator.stopSimulation();
		}
		
		if(allCrossed) {
			fitness = 1 + targetBonus + distanceBonus - penalty + (2*(steps-simulator.getTime())/steps);
			simulator.stopSimulation();
		}
		
	}
	
	private Vector2d getCenterOfMass(ArrayList<Robot> robots) {
		double centerX = 0;
		double centerY = 0;
		for(Robot r : robots) {
			centerX+=r.getPosition().getX();
			centerY+=r.getPosition().getY();
		}
		return new Vector2d(centerX/robots.size(),centerY/robots.size());
	}
}