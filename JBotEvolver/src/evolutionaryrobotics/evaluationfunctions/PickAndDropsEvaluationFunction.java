package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class PickAndDropsEvaluationFunction extends EvaluationFunction {
	private boolean countEvolvingRobotsOnly = false;

	public PickAndDropsEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator, args);
	}

	@Override
	public void update(double time) {
		
		double tempFitness = 0;
		
		for (Robot r : simulator.getEnvironment().getRobots()) {
			if (!countEvolvingRobotsOnly || r.getController() instanceof NeuralNetworkController) {
				PreyPickerActuator actuator = (PreyPickerActuator)r.getActuatorByType(PreyPickerActuator.class);
				tempFitness += actuator.getNumDrops();
			}
		}
		fitness = tempFitness / ((RoundForageEnvironment) (simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() * 1.0;
	}

	public void enableCountEvolvingRobotsOnly() {
		countEvolvingRobotsOnly = true;
	}
}