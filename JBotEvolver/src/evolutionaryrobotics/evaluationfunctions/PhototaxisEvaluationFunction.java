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
	
	public PhototaxisEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		if(r == null) {
			r = simulator.getRobots().get(0);
			for(PhysicalObject o : simulator.getEnvironment().getAllObjects()) {
				if(o instanceof LightPole) {
					light = o;
					break;
				}
			}
			initialDistance = r.getPosition().distanceTo(light.getPosition());
		}
		
		currentDistance = r.getPosition().distanceTo(light.getPosition());
		
		if(currentDistance < 0.05) {
			simulator.stopSimulation();
			currentDistance = 0;
		}
	}
	
	@Override
	public double getFitness() {
		fitness = 10 + (1 - currentDistance/initialDistance);
		return fitness;
	}

}
