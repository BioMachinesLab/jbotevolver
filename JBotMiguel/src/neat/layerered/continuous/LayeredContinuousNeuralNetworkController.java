package neat.layerered.continuous;

import java.util.ArrayList;

import neat.layerered.ANNNeuron;
import neat.layerered.ANNSynapse;
import neat.layerered.LayeredANN;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class LayeredContinuousNeuralNetworkController extends NeuralNetworkController{

	public LayeredContinuousNeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}
	
	@Override
	public void setNeuralNetwork(NeuralNetwork network) {
		LayeredContinuousNeuralNetwork wrapper = (LayeredContinuousNeuralNetwork)network;
		
		LayeredANN copyNetwork =
				new LayeredContinuousNEATCODEC().createNetFromStructure(
						wrapper.getNetwork().getAllSynapses(),
						wrapper.getNetwork().getAllNeurons());
		
		((LayeredContinuousNeuralNetwork)this.neuralNetwork).setNetwork(copyNetwork);
		
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
		
		LayeredANN net = LayeredContinuousNeuralNetwork.getNetworkByWeights(weights);
		
		((LayeredContinuousNeuralNetwork)this.neuralNetwork).setNetwork(net);
		
		reset();
	}
}
