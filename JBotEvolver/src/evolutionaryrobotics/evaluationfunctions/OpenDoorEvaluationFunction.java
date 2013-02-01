package evolutionaryrobotics.evaluationfunctions;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorEvaluationFunction extends EvaluationFunction {
	
	private double fitness = 0;
	private double maxSteps = 300;
	private double timeAlive = 0;
	private double maxDistance = 0;
	
	private boolean debug;
	private double bonus = 0;

	public OpenDoorEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator);
		maxSteps = arguments.getArgumentAsDoubleOrSetDefault("steps", maxSteps);
		debug = arguments.getArgumentAsIntOrSetDefault("debug", 0) == 1;
	}

	@Override
	public double getFitness() {
		return fitness;
	}

	@Override
	public void step() {
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
			simulator.getExperiment().endExperiment();
		}else if(env.doorsOpen) {
			fitness = 1+(maxSteps-timeAlive)/maxSteps;
			simulator.getExperiment().endExperiment();
		}
		
		Robot r = simulator.getEnvironment().getRobots().get(0);
		
		if(debug && env.doorsOpen) {
			if(r.getLeftWheelSpeed() > 0 && r.getRightWheelSpeed() > 0){
				bonus+=0.01;
			}else if(r.getLeftWheelSpeed() < 0 && r.getRightWheelSpeed() < 0){
				bonus-=0.01;
			}
			fitness+=bonus;
		}
	}

}
