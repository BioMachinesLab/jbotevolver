package neat.continuous;

import java.util.ArrayList;
import neat.NEATNetworkController;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

public class NEATContinuousNetworkController extends NEATNetworkController {
	
	public NEATContinuousNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}
	
	@Override
	public void setNeuralNetwork(NeuralNetwork network) {
		ERNEATContinuousNetwork wrapper = (ERNEATContinuousNetwork)network;
		NEATContinuousNetwork copyNetwork = (NEATContinuousNetwork)createCopyNetwork(wrapper.getNEATNetwork());
		((ERNEATContinuousNetwork)this.neuralNetwork).setNEATNetwork(copyNetwork);
		
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

	public static synchronized NEATNetwork createCopyNetwork(NEATNetwork net) {
		
		NEATContinuousNetwork neatNetwork = (NEATContinuousNetwork)net;
		
		//get
		NEATLink[] originalLinks = neatNetwork.getLinks();
		ActivationFunction[] originalFunctions = neatNetwork.getActivationFunctions();
		Neuron[] originalNeurons = neatNetwork.getNeurons();
		
		//initialise
		int inputs = neatNetwork.getInputCount(), outputs = neatNetwork.getOutputCount();
		ArrayList<NEATLink> links = new ArrayList<NEATLink>(originalLinks.length);
		ActivationFunction[] functions = new ActivationFunction[originalFunctions.length];
		ArrayList<NEATNeuronGene> neurons = new ArrayList<NEATNeuronGene>();
		
		//copy/clone
		for(int i = 0; i < originalLinks.length; i++){
			NEATLink original = originalLinks[i];
			links.add(new NEATLink(original.getFromNeuron(), original.getToNeuron(), original.getWeight()));
		}
		
		for(int i = 0; i < originalFunctions.length; i++){
			ActivationFunction original = originalFunctions[i];
			functions[i] = original.clone();
		}
		for(int i = 0 ; i < originalNeurons.length ; i++){
			Neuron original = originalNeurons[i];
			if(original.isDecayNeuron()) {
				neurons.add(new NEATContinuousNeuronGene(NEATNeuronType.values()[original.getType()],new ActivationSteepenedSigmoid(),original.getId(),original.getInnovationId(),original.getDecay(),original.getBias()));
			} else {
				neurons.add(new NEATNeuronGene(NEATNeuronType.values()[original.getType()],new ActivationSteepenedSigmoid(),original.getId(),original.getInnovationId()));
			}
		}
		return new NEATContinuousNetwork(inputs, outputs, links, functions,neurons);
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
		
		NEATContinuousNetwork net = ERNEATContinuousNetwork.getNetworkByWeights(weights);
		
		((ERNEATContinuousNetwork)this.neuralNetwork).setNEATNetwork(net);
		reset();
	}

}
