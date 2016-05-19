package evaluationfunctions;

import java.util.ArrayList;
import mathutils.Vector2d;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CrossRoomsAggregatedEvaluationFunction extends EvaluationFunction {
	
	private double targetX = 0;
	private double steps = 0;
	private boolean punishCollision = true;
	private boolean awardBest = false;
	private boolean debug = false;
	private double startingX = 0;
	private double penalty = 0;
	private boolean multicross = false;
	private boolean closeRobots = false;
	
	private double distanceBonus = 0;

	public CrossRoomsAggregatedEvaluationFunction(Arguments arguments) {
		super(arguments);
		targetX = arguments.getArgumentAsDoubleOrSetDefault("targetx", targetX);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		awardBest = arguments.getArgumentAsIntOrSetDefault("awardbest", 0) == 1;
		debug = arguments.getArgumentAsIntOrSetDefault("debug", 0) == 1;
		multicross = arguments.getArgumentAsIntOrSetDefault("multicross", 0) == 1;
		closeRobots = arguments.getArgumentAsIntOrSetDefault("closerobots", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
		
		Vector2d center = getCenterOfMass(simulator.getRobots());
		
		if(debug && simulator.getTime() == 0) {
			startingX = center.getX();
		}
		
		double robotPos = center.getX();
		double totalDistance = Math.abs(targetX)*2.0;
		double currentDistance = Math.abs(targetX-robotPos);
		
		if(debug) {
			totalDistance = Math.abs(startingX);
			currentDistance = Math.abs(targetX-robotPos);
		}
		
		double percentage = (totalDistance-currentDistance)/totalDistance;
		
		double prevFitness = fitness;
			
		fitness = percentage;
		if(punishCollision){
			for(Robot r : simulator.getRobots()) {
				if(r.isInvolvedInCollison()) {
					penalty+= 0.01/simulator.getRobots().size();
	//				simulator.stopSimulation();
				}
			}
		}
		
		if(closeRobots) {
			ArrayList<Robot> robots = simulator.getRobots();
			int number = 0;
			for(int i = 0 ; i <  robots.size() ; i++) {
				for(int j = i+1 ; j < robots.size() ; j++) {
					double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
					number++;
					if(distance < 1)
						distanceBonus+= (1-distance)/simulator.getEnvironment().getSteps();
				}
			}
		}
		
		boolean allCrossed = false;
		
		if(multicross) {
			allCrossed = true;
			for(Robot r : simulator.getRobots()) {
				if(r.getPosition().getX() >= 0) {
					allCrossed = false;
					break;
				}
			}
		}
		
		if(percentage > 0.85 || (multicross && allCrossed)) {
			fitness = 1;
			fitness+= 5*((steps-simulator.getTime())/steps);
			if(closeRobots) {
				fitness+= 5*distanceBonus;
			}
			
			if(punishCollision)
				fitness-=penalty;
			simulator.stopSimulation();
		}
		
		if(awardBest && prevFitness > fitness)
			fitness = prevFitness;
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