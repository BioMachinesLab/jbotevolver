package roommaze;

import java.util.LinkedList;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorEvaluationFunction extends EvaluationFunction {
	
	private double maxSteps = 300;
	private double timeAlive = 0;
	private double maxDistance = 0;
	private boolean debug;
	private double bonus = 0;

	public OpenDoorEvaluationFunction(Arguments arguments) {
		super(arguments);
		maxSteps = arguments.getArgumentAsDoubleOrSetDefault("steps", maxSteps);
		debug = arguments.getArgumentAsIntOrSetDefault("debug", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		timeAlive++;
		TwoRoomsEnvironment env = ((TwoRoomsEnvironment)simulator.getEnvironment());
		LinkedList<Wall> buttons = env.getButtons();
		Vector2d robotPos = simulator.getEnvironment().getRobots().get(0).getPosition();
		
		double closestDistance = 100000;
		
		for(Wall w : buttons) {
			
			double totalDist = w.getPosition().distanceTo(robotPos);
			
			if(totalDist < closestDistance)
				closestDistance = totalDist;
			if(totalDist > maxDistance)
				maxDistance = totalDist;
		}
		
		fitness = maxDistance-closestDistance;
		
		if(simulator.getEnvironment().getRobots().get(0).isInvolvedInCollison()) {
			fitness/=2;
			simulator.stopSimulation();
		}else if(env.doorsOpen) {
			fitness = 1+(maxSteps-timeAlive)/maxSteps;
			simulator.stopSimulation();
		}
		
		Robot r = simulator.getEnvironment().getRobots().get(0);
		
		if(debug && env.doorsOpen) {
			DifferentialDriveRobot ddr = (DifferentialDriveRobot)r;
			if(ddr.getLeftWheelSpeed() > 0 && ddr.getRightWheelSpeed() > 0){
				bonus+=0.01;
			}else if(ddr.getLeftWheelSpeed() < 0 && ddr.getRightWheelSpeed() < 0){
				bonus-=0.01;
			}
			fitness+=bonus;
		}
	}
}