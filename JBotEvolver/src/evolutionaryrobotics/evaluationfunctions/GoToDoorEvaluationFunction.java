package evolutionaryrobotics.evaluationfunctions;

import java.util.LinkedList;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GoToDoorEvaluationFunction extends EvaluationFunction {
	
	private double targetX = 0;
	private double steps = 0;
	private boolean punishCollision = true;
	private boolean openDoor = false;
	private double startingX = 0;
	private TwoRoomsEnvironment env;
	private Wall closestWallButton;

	public GoToDoorEvaluationFunction(Arguments arguments) {
		super(arguments);
//		targetX = arguments.getArgumentAsDoubleOrSetDefault("targetx", targetX);
		steps = arguments.getArgumentAsDoubleOrSetDefault("steps", steps);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		openDoor = arguments.getArgumentAsIntOrSetDefault("opendoor", 0) == 1;
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
		
		Robot r = env.getRobots().get(0);
		
		double robotPos = env.getRobots().get(0).getPosition().getX();
		double totalDistance = Math.abs(startingX - targetX);
		double currentDistance = Math.abs(targetX-robotPos);
				
		double percentage = (totalDistance-currentDistance)/totalDistance;
		
		fitness = percentage;
		
		if(env.getRobots().get(0).isInvolvedInCollison()) {
			if(punishCollision){
				fitness=-1;
			}
			simulator.stopSimulation();
		}
		
		double robotY = env.getRobots().get(0).getPosition().getY();
		
		if((openDoor && env.doorsOpen) || (!openDoor && getHorizontalDistanceToWall(r,closestWallButton) < 0.1 && Math.abs(robotY) < 0.1)) {
			fitness = 1 + ((steps-simulator.getTime())/steps);
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