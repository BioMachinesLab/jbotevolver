package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class PickAndDropsEvaluationFunction extends EvaluationFunction {
	private double fitness;
	private boolean countEvolvingRobotsOnly = false;
	private boolean hadPrey = false;

	public PickAndDropsEvaluationFunction(Simulator simulator) {
		super(simulator);
	}

	public double getFitness() {

		for (Robot r : simulator.getEnvironment().getRobots()) {
			if (!countEvolvingRobotsOnly
					|| r.getController().getClass()
							.equals(NeuralNetworkController.class)) {
				fitness += r.getNumDrops();
			}
		}

		return fitness
				/ ((RoundForageEnvironment) (simulator.getEnvironment()))
						.getNumberOfFoodSuccessfullyForaged() * 1.0;
	}

	// @Override
	public void step() {

	}

	public void enableCountEvolvingRobotsOnly() {
		countEvolvingRobotsOnly = true;
	}
}
