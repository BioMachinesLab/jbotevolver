package neat;

import java.util.ArrayList;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class NEATNetworkController extends NeuralNetworkController implements VariableLengthEvolvableController<NEATNetwork> {

	public NEATNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}
	
	@Override
	public void setNeuralNetwork(NeuralNetwork network) {
		ERNEATNetwork wrapper = (ERNEATNetwork)network;
		NEATNetwork copyNetwork = createCopyNetwork(wrapper.getNetwork());
		((ERNEATNetwork)this.neuralNetwork).setNetwork(copyNetwork);
		
		if(printWeights) {
			double[] weights = neuralNetwork.getWeights();
			String w = "#weights (total of "+weights.length+")\n";
			for(int i = 0 ; i < weights.length ; i++) {
				w+=weights[i];
				if(i != weights.length-1)
					w+=",";
			}
			System.out.println(w);
		}
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
	public void setNNWeights(double[] weights) {
		
		if(printWeights) {
			String w = "#weights (total of "+weights.length+")\n";
			for(int i = 0 ; i < weights.length ; i++) {
				w+=weights[i];
				if(i != weights.length-1)
					w+=",";
			}
			System.out.println(w);
		}
		
		NEATNetwork net = ERNEATNetwork.getNetworkByWeights(weights);
		
		((ERNEATNetwork)this.neuralNetwork).setNetwork(net);
		reset();
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
