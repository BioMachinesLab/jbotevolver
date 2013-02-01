package evolutionaryrobotics.evaluationfunctions;

import experiments.FoodWaterExperiment;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;

public class FoodWaterEvaluationFunction extends EvaluationFunction{
	private int numberOfRobotStepsOutsideArena = 0;
	
	
	public FoodWaterEvaluationFunction(Simulator simulator) {
		super(simulator);
	}

	//@Override
	public double getFitness() {
		int numberOfRecharges = 0;
		
		for(Robot r : simulator.getEnvironment().getRobots()) {
			numberOfRecharges += r.getParameterAsInteger(FoodWaterExperiment.RECHARGED).intValue();
		}
			
		return (double) numberOfRecharges - ((double) numberOfRobotStepsOutsideArena) / 100.0;
	}

	//@Override
	public void step() {			
		Environment e = simulator.getEnvironment();
		for(Robot r : simulator.getEnvironment().getRobots()){
			Vector2d p = r.getPosition();
			
			if (p.x < -e.getWidth() / 2 || 
				p.x > e.getWidth() / 2 ||
				p.y < -e.getHeight() / 2 || 
				p.y > e.getHeight() / 2)
				numberOfRobotStepsOutsideArena++;
		}
			
	}
}
