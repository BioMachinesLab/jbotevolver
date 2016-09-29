package roommaze;

import java.util.LinkedList;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GoToButtonEvaluationFunction extends EvaluationFunction {
	
	private double targetX = 0;
	private double steps = 0;
	private boolean punishCollision = true;
	private boolean openDoor = false;
	private double startingX = 0;
	private TwoRoomsEnvironment env;
	private Wall closestWallButton;
	private double penalty = 0;
	private boolean kill = false;
	private boolean dead = false;

	public GoToButtonEvaluationFunction(Arguments arguments) {
		super(arguments);
//		targetX = arguments.getArgumentAsDoubleOrSetDefault("targetx", targetX);
		steps = arguments.getArgumentAsDoubleOrSetDefault("steps", steps);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		openDoor = arguments.getArgumentAsIntOrSetDefault("opendoor", 0) == 1;
		kill = arguments.getArgumentAsIntOrSetDefault("kill", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(simulator.getTime() == 0) {
			env = ((TwoRoomsEnvironment)simulator.getEnvironment());
			startingX = 1;//env.getRobots().get(0).getPosition().getX();
			LinkedList<Wall> buttons = env.getButtons();
			closestWallButton = findClosestWallButton(buttons,env.getRobots().get(0));
			targetX = closestWallButton.getPosition().getX();
		}
		
		Robot closestRobot = simulator.getEnvironment().getRobots().get(0);
		double bestPercentage = -1000;
		
		for(Robot r : simulator.getEnvironment().getRobots()) {
		
			double robotPos = r.getPosition().getX();
			double totalDistance = Math.abs(startingX - targetX);
			double currentDistance = Math.abs(targetX-robotPos);
					
			double percentage = (totalDistance-currentDistance)/totalDistance;
			
			if(bestPercentage < percentage) {
				bestPercentage = percentage;
				closestRobot = r;
				fitness = percentage;
			}
			
			if(r.isInvolvedInCollison()) {
				if(punishCollision){
					penalty-=0.0005;
				}
				if(kill) {
					simulator.stopSimulation();
					penalty-=1;
					fitness+=penalty;
					dead = true;
				}
			}
		}
		
		if(!dead && (openDoor && env.doorsOpen) || (!openDoor && getHorizontalDistanceToWall(closestRobot,closestWallButton) < 0.15 && Math.abs(closestWallButton.getPosition().getY()-closestRobot.getPosition().getY()) < 0.1)) {
			fitness = 1 + ((steps-simulator.getTime())/steps) + penalty;
			simulator.stopSimulation();
		}
	}
	
	private double getHorizontalDistanceToWall(Robot r, Wall w) {
		return Math.abs(r.getPosition().getX()-w.getPosition().getX());
	}

	private Wall findClosestWallButton(LinkedList<Wall> buttons, Robot robot) {
		
		int minIndex = 0;
		double x = robot.getPosition().getX();
		
		for(int i = 1 ; i < buttons.size() ; i++) {
			Wall w = buttons.get(i);
			double wx = w.getPosition().getX();
			if(Math.abs(x-wx) < Math.abs(x-buttons.get(minIndex).getPosition().getX()))
				minIndex = i;
		}
		return buttons.get(minIndex);
	}
}