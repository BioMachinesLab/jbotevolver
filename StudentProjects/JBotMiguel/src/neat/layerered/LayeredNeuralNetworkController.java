package neat.layerered;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class LayeredNeuralNetworkController extends NeuralNetworkController{

	public LayeredNeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}
	
	@Override
	public void setNeuralNetwork(NeuralNetwork network) {
		LayeredNeuralNetwork wrapper = (LayeredNeuralNetwork)network;
		
		LayeredANN copyNetwork = wrapper.getNetwork().copy();
		
		((LayeredNeuralNetwork)this.neuralNetwork).setNetwork(copyNetwork);
		
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
		
		LayeredANN net = LayeredNeuralNetwork.getNetworkByWeights(weights);
		
		((LayeredNeuralNetwork)this.neuralNetwork).setNetwork(net);
		
		reset();
	}
}
