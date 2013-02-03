package evolutionaryrobotics.evaluationfunctions;

import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class PickAndDropsEvaluationFunction extends EvaluationFunction {
	private boolean countEvolvingRobotsOnly = false;

	public PickAndDropsEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator, args);
		countEvolvingRobotsOnly = args.getArgumentIsDefined("countevolvingrobotsonly");
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
}