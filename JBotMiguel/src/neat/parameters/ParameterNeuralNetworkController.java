package neat.parameters;

import neat.layerered.LayeredANN;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class ParameterNeuralNetworkController extends NeuralNetworkController{

	public ParameterNeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
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
		
		LayeredANN net = ParameterNeuralNetwork.getNetworkByWeights(weights);
		
		((ParameterNeuralNetwork)this.neuralNetwork).setNetwork(net);
		
		reset();
	}
}
