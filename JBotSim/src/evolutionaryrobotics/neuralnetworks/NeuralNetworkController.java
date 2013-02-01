package evolutionaryrobotics.neuralnetworks;

import java.util.LinkedList;
import java.util.Vector;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class NeuralNetworkController extends Controller implements FixedLenghtGenomeEvolvableController {
	protected NeuralNetwork neuralNetwork;

	public NeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		Vector<NNInput> inputs = simulator.getControllerFactory().getNNInputs(robot, args);
		Vector<NNOutput> outputs = simulator.getControllerFactory().getNNOutputs(robot, args);

		neuralNetwork = new MulitlayerPerceptron(inputs, outputs, args);
		simulator.getControllerFactory().setChromosomeLenght(getRequiredNumberOfWeights());
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

	public int getRequiredNumberOfWeights() {
		return neuralNetwork.getRequiredNumberOfWeights();
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
		//		setWeights(weights);		
	}

	public static void setNNWeights(LinkedList<Robot> robots, double[] weights) {
		for (Robot r : robots) {
			if (r.getController() instanceof FixedLenghtGenomeEvolvableController){
				FixedLenghtGenomeEvolvableController nnController = (FixedLenghtGenomeEvolvableController) r.getController();
				if (nnController != null)
					nnController.setNNWeights(weights);
			} //TODO: Miguel: you have to get the BehaviorController to implement the FixedLengthGenomeEvolvableController so that 
			//              weights can be set tranparently.
			//			else if (r.getEvolvingController() instanceof BehaviorController){
			//				BehaviorController bController       = (BehaviorController) r.getEvolvingController();
			//				NeuralNetworkController nnController = (NeuralNetworkController) bController.getEvolvingController();
			//				nnController.setNNWeights(weights);
		}
	}	
}
