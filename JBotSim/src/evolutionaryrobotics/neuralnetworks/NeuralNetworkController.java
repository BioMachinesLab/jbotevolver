package evolutionaryrobotics.neuralnetworks;

import java.util.LinkedList;
import java.util.Vector;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import factories.ControllerFactory;

public class NeuralNetworkController extends Controller implements FixedLenghtGenomeEvolvableController {
	protected NeuralNetwork neuralNetwork;

	public NeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		Vector<NNInput> inputs = ControllerFactory.getNNInputs(simulator, robot, args);
		Vector<NNOutput> outputs = ControllerFactory.getNNOutputs(simulator, robot, args);
		
		String name = args.getArgumentAsString("network");

		if (name.equalsIgnoreCase("MultilayerPerceptron")) {
			neuralNetwork = new MulitlayerPerceptron(inputs, outputs, args);
		} else if (name.equalsIgnoreCase("CTRNNMultilayer")) {
			neuralNetwork = new CTRNNMultilayer(inputs, outputs, args);
		}
		
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