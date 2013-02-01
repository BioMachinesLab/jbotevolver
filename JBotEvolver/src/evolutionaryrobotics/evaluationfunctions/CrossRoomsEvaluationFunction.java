package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.util.Arguments;

public class CrossRoomsEvaluationFunction extends EvaluationFunction {
	
	private double fitness = 0;
	private double targetX = 0;
	private double time = 0;
	private double steps = 0;
	private boolean punishCollision = true;
	private boolean awardBest = false;
	
	private boolean debug = false;
	private double startingX = 0;

	public CrossRoomsEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator);
		targetX = arguments.getArgumentAsDoubleOrSetDefault("targetx", targetX);
		steps = arguments.getArgumentAsDoubleOrSetDefault("steps", steps);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		awardBest = arguments.getArgumentAsIntOrSetDefault("awardbest", 0) == 1;
		debug = arguments.getArgumentAsIntOrSetDefault("debug", 0) == 1;
		
	}

	@Override
	public double getFitness() {
		return fitness;
	}

	@Override
	public void update(double time) {
		
		if(debug && time == 0) {
			startingX = simulator.getEnvironment().getRobots().get(0).getPosition().getX();
		}
		
		time++;
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
			simulator.getExperiment().endExperiment();
		}
		
		if(percentage > 0.85) {
			fitness = 1;
			fitness+= 10*((steps-time)/steps);
			simulator.getExperiment().endExperiment();
		}
		
		if(awardBest && prevFitness > fitness)
			fitness = prevFitness;
	}

}
