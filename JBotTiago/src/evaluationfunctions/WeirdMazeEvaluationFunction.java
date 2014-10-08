package evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.WeirdMazeEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class WeirdMazeEvaluationFunction extends EvaluationFunction {
	
	private static final int LIMIT = 5;
	private WeirdMazeEnvironment environment;
	private double penalty;
	
	public WeirdMazeEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		
		environment = (WeirdMazeEnvironment) simulator.getEnvironment();
		LightPole light = environment.getLightPole();

		double maxDistX = 0;
		penalty = 0;
		
		for (Robot robot : environment.getRobots()) {
			maxDistX = Math.max(maxDistX, (Math.abs(robot.getPosition().getX() - light.getPosition().getX())));
		}
		
		fitness = ((LIMIT-maxDistX)/LIMIT);
		
		for (Robot robot : environment.getRobots()) {
			if(robot.isInvolvedInCollison())
				penalty += 2/environment.getRobots().size()/environment.getSteps();
		}
		
		if(maxDistX < 0.2){
			fitness += (1 - (simulator.getTime()/environment.getSteps()));
			simulator.stopSimulation();
		}
		
	}

	@Override
	public double getFitness() {
		return super.getFitness() - penalty;
	}
	
}
