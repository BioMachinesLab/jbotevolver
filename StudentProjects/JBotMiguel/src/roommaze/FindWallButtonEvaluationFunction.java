package roommaze;

import java.util.LinkedList;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class FindWallButtonEvaluationFunction extends EvaluationFunction {
	
	private double maxSteps = 300;
	private double timeAlive = 0;

	public FindWallButtonEvaluationFunction(Arguments arguments) {
		super(arguments);
		maxSteps = arguments.getArgumentAsDoubleOrSetDefault("steps", maxSteps);
	}

	@Override
	public void update(Simulator simulator) {
		timeAlive++;
		LinkedList<Wall> buttons = ((TwoRoomsEnvironment)simulator.getEnvironment()).getButtons();
		Vector2d robotPos = simulator.getEnvironment().getRobots().get(0).getPosition();
		
		double closestDistance = 100000;
		
		for(Wall w : buttons) {
			double yDistance = Math.abs(robotPos.y-w.getPosition().y);
			double xDistance = Math.abs(robotPos.x-w.getPosition().x);
			if(xDistance+yDistance < closestDistance)
				closestDistance = xDistance+yDistance;
		}
		
		fitness = 1-closestDistance;
		
		if(simulator.getEnvironment().getRobots().get(0).isInvolvedInCollison()) {
			if(fitness > 0.8){
				fitness+= (maxSteps-timeAlive)/maxSteps;
			}
			simulator.stopSimulation();
		}
	}
}