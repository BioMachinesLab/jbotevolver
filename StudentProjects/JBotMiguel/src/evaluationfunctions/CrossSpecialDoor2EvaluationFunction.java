package evaluationfunctions;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CrossSpecialDoor2EvaluationFunction extends EvaluationFunction {
	
	private double steps = 0;
	private boolean punishCollision = true;

	public CrossSpecialDoor2EvaluationFunction(Arguments arguments) {
		super(arguments);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
		
		double nRobots = simulator.getRobots().size();
		
		int crossed = 0;
		
		for(Robot r : simulator.getRobots()) {
			
			if(punishCollision && r.isInvolvedInCollison())
				continue;
			
			double x = r.getPosition().getX();
			
			if(x > 0.3 && x < 2)
				fitness+= (2-Math.abs(x))/steps/nRobots;
			else if(x < 0.3 && x > - 0.3) 
				fitness+= 2/steps/nRobots;
			else if(x < -0.4) {
				fitness+=3/steps/nRobots;
				crossed++;
			}
		}
		
		if(crossed == nRobots) {
			fitness+=3;
			simulator.stopSimulation();
		}
	}
}