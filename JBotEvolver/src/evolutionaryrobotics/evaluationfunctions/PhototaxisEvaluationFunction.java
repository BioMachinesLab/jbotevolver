package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PhototaxisEvaluationFunction extends EvaluationFunction {

	private Robot r = null;
	private PhysicalObject light = null;
	private double initialDistance = 0;
	private double currentDistance = 0;
	
	private boolean countTime = false;
	private int totalSteps = 0;
	private double currentSteps = 0;
	
	public PhototaxisEvaluationFunction(Arguments args) {
		super(args);
		countTime = args.getFlagIsTrue("counttime");
	}

	@Override
	public void update(Simulator simulator) {
		if(r == null) {
			r = simulator.getRobots().get(0);
			totalSteps = simulator.getEnvironment().getSteps();
			for(PhysicalObject o : simulator.getEnvironment().getAllObjects()) {
				if(o instanceof LightPole) {
					light = o;
					break;
				}
			}
			initialDistance = r.getPosition().distanceTo(light.getPosition());
		}
		
		currentSteps = simulator.getTime();
		
		currentDistance = r.getPosition().distanceTo(light.getPosition());
		
		if(currentDistance < 0.05) {
			simulator.stopSimulation();
			currentDistance = 0;
		}
	}
	
	@Override
	public double getFitness() {
		
		double timeFactor = 0;
		
		if(countTime && currentDistance == 0) {
			timeFactor = 1.0 - currentSteps/totalSteps;
		}
		
		fitness = 10.0 + (1.0 - currentDistance/initialDistance) + timeFactor;
		return fitness;
	}

}
