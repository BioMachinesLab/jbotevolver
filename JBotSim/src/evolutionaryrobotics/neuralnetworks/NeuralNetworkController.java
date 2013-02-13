package evolutionaryrobotics.neuralnetworks;

import java.util.LinkedList;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;

public class NeuralNetworkController extends Controller implements FixedLenghtGenomeEvolvableController {
	protected NeuralNetwork neuralNetwork;

	public NeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		neuralNetwork = NeuralNetwork.getNeuralNetwork(simulator, robot, new Arguments(args.getArgumentAsString("network")));
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
	}

	public boolean isAlive() {
		return true;
	}

	@Override
	public void begin() {
	}

	@Override
	public void controlStep(double time) {
		neuralNetwork.controlStep(time);
	}

	@Override
	public void end() {
	}

	@Override
	public int getGenomeLength() {
		return neuralNetwork.getGenomeLength();
	}

	public NeuralNetwork getNeuralNetwork() {
		return neuralNetwork;
	}

	@Override
	public void reset() {
		super.reset();
		neuralNetwork.reset();
	}

	@Override
	public void setNNWeights(double[] weights) {
		neuralNetwork.setWeights(weights);
	}
	
	@Override
	public double[] getNNWeights() {
		return neuralNetwork.getWeights();
	}

	public static void setNNWeights(LinkedList<Robot> robots, double[] weights) {
		for (Robot r : robots) {
			if (r.getController() instanceof FixedLenghtGenomeEvolvableController){
				FixedLenghtGenomeEvolvableController nnController = (FixedLenghtGenomeEvolvableController) r.getController();
				if (nnController != null)
					nnController.setNNWeights(weights);
			}
		}
	}	
}