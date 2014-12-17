package arbitrators;

import neat.layerered.ANNNeuron;
import neat.layerered.LayeredANN;
import neat.parameters.ParameterNeuralNetwork;
import neat.parameters.ParameterNeuron;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.BehaviorController;
import controllers.Controller;
import controllers.ParameterLocomotionController;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

public class MutableBehaviorArbitrator extends BehaviorController {
	
	private boolean configured = false;

	public MutableBehaviorArbitrator(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}
	
	@Override
	public void controlStep(double time) {
		if(!configured) {
			ParameterNeuralNetwork net = (ParameterNeuralNetwork)neuralNetwork;
			//Setting up the sub-controllers
			
			int index = 0;
			
			for(ANNNeuron n : net.getNetwork().getAllNeurons()) {
				if(n instanceof ParameterNeuron) {
					double p = ((ParameterNeuron)n).getParameter();
					((ParameterLocomotionController)(subControllers.get(index++))).setParameter(p);
				}
			}
			
			configured = true;
		}
		super.controlStep(time);
	}
	
	@Override
	protected void setupControllers(Simulator simulator, Arguments args) {
		
		//Setting up main controller
		neuralNetwork = (NeuralNetwork)NeuralNetwork.getNeuralNetwork(simulator, robot, new Arguments(args.getArgumentAsString("network")));
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
		
		if(!(neuralNetwork instanceof ParameterNeuralNetwork))
			throw new RuntimeException("This controller is not ready for any other type of network!");
		
		for(int i = 0 ; i < neuralNetwork.getOutputNeuronStates().length ; i++) {
			ParameterLocomotionController c = new ParameterLocomotionController(simulator, robot, new Arguments(""));
			subControllers.add(c);
		}
		
		parallelController = new boolean[neuralNetwork.getOutputNeuronStates().length];
		
		resetChosen = args.getArgumentAsIntOrSetDefault("resetchosen", 1) == 1;
		keepFeeding = args.getArgumentAsIntOrSetDefault("keepfeeding", 0) == 1;
		debugMax = args.getArgumentAsIntOrSetDefault("debugmax", 0) == 1;
	}
	
}
