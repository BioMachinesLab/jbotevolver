package roommaze;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

public class CrossRoomsEvaluationFunction extends EvaluationFunction {
	
	private double targetX = 0;
	private double steps = 0;
	private boolean punishCollision = true;
	private boolean awardBest = false;
	private boolean debug = false;
	private double startingX = 0;
	private boolean kill = false;

	public CrossRoomsEvaluationFunction(Arguments arguments) {
		super(arguments);
		targetX = arguments.getArgumentAsDoubleOrSetDefault("targetx", targetX);
		steps = arguments.getArgumentAsDoubleOrSetDefault("steps", steps);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		awardBest = arguments.getArgumentAsIntOrSetDefault("awardbest", 0) == 1;
		debug = arguments.getArgumentAsIntOrSetDefault("debug", 0) == 1;
		kill = arguments.getArgumentAsIntOrSetDefault("kill", 1) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(debug && simulator.getTime() == 0) {
			startingX = simulator.getEnvironment().getRobots().get(0).getPosition().getX();
		}
		
		double robotPos = simulator.getEnvironment().getRobots().get(0).getPosition().getX();
		double totalDistance = Math.abs(targetX)*2.0;
		double currentDistance = Math.abs(targetX-robotPos);
		
		if(debug) {
			totalDistance = Math.abs(startingX);
			currentDistance = Math.abs(targetX-robotPos);
		}
		
		double percentage = (totalDistance-currentDistance)/totalDistance;
		
		double prevFitness = fitness;
			
		fitness = percentage;
		
		if(simulator.getEnvironment().getRobots().get(0).isInvolvedInCollison()) {
			if(punishCollision && percentage < 0.5){
				fitness=-1;
			}
			if(punishCollision || kill)
				simulator.stopSimulation();
		}
		
		if(percentage > 0.85) {
			fitness = 1;
			fitness+= 10*((steps-simulator.getTime())/steps);
			simulator.stopSimulation();
		}
		
		if(awardBest && prevFitness > fitness)
			fitness = prevFitness;
	}
}