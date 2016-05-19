package roommaze;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

public class TwoRoomsEvaluationFunction extends EvaluationFunction {
	
	private int picks = 0;
	private double timeAlive = 0;
	private double maxNumberOfSteps = 500;
	private boolean punishCollision = true;
	private boolean allowCollision = false;
	private boolean rewardStepsAlive = true;
	private boolean zeroFitnessCollision = false;
	private double lastX = 0;
	

	public TwoRoomsEvaluationFunction(Arguments arguments) {
		super(arguments);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		allowCollision = arguments.getArgumentAsIntOrSetDefault("allowcollision", 0) == 1;
		zeroFitnessCollision = arguments.getArgumentAsIntOrSetDefault("zerofitnesscollision", 0) == 1;
		
		rewardStepsAlive = arguments.getArgumentAsIntOrSetDefault("rewardstepsalive", 1) == 1;
		
		maxNumberOfSteps = arguments.getArgumentAsDoubleOrSetDefault("maxnumberofsteps", maxNumberOfSteps);
	}

	@Override
	public void update(Simulator simulator) {
		
		if(timeAlive == 0)
			lastX = simulator.getRobots().get(0).getPosition().getX();
		else {
			double currentX = simulator.getRobots().get(0).getPosition().getX();
			
			lastX = currentX;
		}
		
		timeAlive++;
		picks = ((TwoRoomsEnvironment)simulator.getEnvironment()).getNumberOfPicks();
		
		fitness = picks;
		
		if(rewardStepsAlive)
			fitness+= (timeAlive/maxNumberOfSteps)*10;
		
		if(!allowCollision && simulator.getEnvironment().getRobots().get(0).isInvolvedInCollison()) {
			if(punishCollision)
				fitness /=  2;
			if(zeroFitnessCollision)
				fitness = 0;
			simulator.stopSimulation();
		}
	}
}