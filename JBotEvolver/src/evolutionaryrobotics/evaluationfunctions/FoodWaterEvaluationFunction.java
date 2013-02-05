package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FoodWaterEvaluationFunction extends EvaluationFunction{
	private int numberOfRobotStepsOutsideArena = 0;
	
	public FoodWaterEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator, args);
	}

	@Override
	public void update(double time) {			
		Environment e = simulator.getEnvironment();
		for(Robot r : simulator.getEnvironment().getRobots()){
			Vector2d p = r.getPosition();
			
			if (p.x < -e.getWidth() / 2 || 
				p.x > e.getWidth() / 2 ||
				p.y < -e.getHeight() / 2 || 
				p.y > e.getHeight() / 2)
				numberOfRobotStepsOutsideArena++;
		}
		int numberOfRecharges = 0;
//		TODO I commented this: (Miguel)
//		for(Robot r : simulator.getEnvironment().getRobots())
//			numberOfRecharges += r.getParameterAsInteger(FoodWaterExperiment.RECHARGED).intValue();
			
		fitness = (double) numberOfRecharges - ((double) numberOfRobotStepsOutsideArena) / 100.0;
	}
}