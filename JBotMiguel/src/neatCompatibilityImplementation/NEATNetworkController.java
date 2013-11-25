package neatCompatibilityImplementation;

import java.util.ArrayList;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;

import controllers.Controller;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class NEATNetworkController extends Controller implements VariableLengthEvolvableController<NEATNetwork> {

	protected ERNEATNetwork neuralNetwork;

	public NEATNetworkController(Simulator simulator, Robot robot,
			Arguments args) {
		super(simulator, robot, args);

		neuralNetwork = (ERNEATNetwork) NeuralNetwork.getNeuralNetwork(simulator, robot, 
				new Arguments(args.getArgumentAsString("network")));
	}
	
	@Override
	public void setNetwork(NEATNetwork network) {
		NEATNetwork copyNetwork = createCopyNetwork(network);
		this.neuralNetwork.setNEATNetwork(copyNetwork);
	}

	public static synchronized NEATNetwork createCopyNetwork(NEATNetwork neatNetwork) {
		//get
		NEATLink[] originalLinks = neatNetwork.getLinks();
		ActivationFunction[] originalFunctions = neatNetwork.getActivationFunctions();
		
		//initialise
		int inputs = neatNetwork.getInputCount(), outputs = neatNetwork.getOutputCount();
		ArrayList<NEATLink> links = new ArrayList<NEATLink>(originalLinks.length);
		ActivationFunction[] functions = new ActivationFunction[originalFunctions.length];
		
		//copy/clone
		for(int i = 0; i < originalLinks.length; i++){
			NEATLink original = originalLinks[i];
			links.add(new NEATLink(original.getFromNeuron(), original.getToNeuron(), original.getWeight()));
		}
		
		for(int i = 0; i < originalFunctions.length; i++){
			ActivationFunction original = originalFunctions[i];
			functions[i] = original.clone();
		}
		
		return new NEATNetwork(inputs, outputs, links, functions);
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
	public void reset() {
		super.reset();
		neuralNetwork.reset();
	}

	public NeuralNetwork getNeuralNetwork() {
		return neuralNetwork;
	}
}
