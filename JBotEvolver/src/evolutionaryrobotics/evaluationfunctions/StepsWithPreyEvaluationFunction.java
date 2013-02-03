package evolutionaryrobotics.evaluationfunctions;

import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class StepsWithPreyEvaluationFunction extends EvaluationFunction{
	private boolean     countEvolvingRobotsOnly = false;
	
	public StepsWithPreyEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		countEvolvingRobotsOnly = arguments.getFlagIsTrue("countevolvingrobotsonly");
	}

	@Override
	public void update(double time) {				
		int robotsWithPrey = 0;
		int robotsCounted  = 0;
		
		for(Robot r : simulator.getEnvironment().getRobots()){
			if (!countEvolvingRobotsOnly || r.getController().getClass().equals(NeuralNetworkController.class)) { 
				if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried())
					robotsWithPrey++;
				
				robotsCounted++;
			}
		}
		fitness+= (double) robotsWithPrey / (double) robotsCounted;
	}

	public void enableCountEvolvingRobotsOnly() {
		    countEvolvingRobotsOnly  = true;
	}
}