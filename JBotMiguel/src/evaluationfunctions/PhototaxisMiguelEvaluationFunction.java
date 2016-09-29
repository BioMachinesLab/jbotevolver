package evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PhototaxisMiguelEvaluationFunction extends EvaluationFunction {
	
	private double initialDistance = 0;
	private LightPole light; 
	
	public PhototaxisMiguelEvaluationFunction(Arguments args) {
		super(args);
	}
	
	@Override
	public void update(Simulator simulator) {
		
		if(simulator.getTime() == 0) {
			for(PhysicalObject p : simulator.getEnvironment().getAllObjects()) {
				if(p.getType() == PhysicalObjectType.LIGHTPOLE) {
					light = (LightPole)p;
					break;
				}
			}
			initialDistance = simulator.getRobots().get(0).getPosition().distanceTo(light.getPosition());
		}
		
		double currentDistance = simulator.getRobots().get(0).getPosition().distanceTo(light.getPosition());
		
		fitness = 1 - (currentDistance/initialDistance);
		
		if(currentDistance < 0.15) {
			simulator.stopSimulation();
			fitness = 2 - simulator.getTime()/simulator.getEnvironment().getSteps();
		}
	}
}