package neatextended;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import controllers.ParameterController;
import evolutionaryrobotics.evolution.neat.NEATNeuralNetwork;
import evolutionaryrobotics.evolution.neat.core.NEATFeatureGene;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class ParameterNeuralNetworkController extends NeuralNetworkController implements FixedLenghtGenomeEvolvableController {
	
	protected NEATFeatureGene[] features;
	protected Controller[] controllers;

	public ParameterNeuralNetworkController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		NEATNeuralNetwork network = ((NEATNeuralNetwork)this.neuralNetwork);
		
		features = new NEATFeatureGene[network.getNumberOfOutputNeurons()];
		controllers = new Controller[network.getNumberOfOutputNeurons()];
		
		String sub = args.getArgumentAsString("subcontrollers");
		Arguments subArgs = new Arguments(sub);
		
		for(int i = 0 ; i < subArgs.getNumberOfArguments() ; i++) {
			Arguments cArgs = new Arguments(subArgs.getArgumentValue(subArgs.getArgumentAt(i)));
			controllers[i] = Controller.getController(simulator, robot, cArgs);
		}
		
	}
	
	@Override
	public void setNNWeights(double[] weights) {
		super.setNNWeights(weights);
		
		NEATNeuralNetwork network = ((NEATNeuralNetwork)this.neuralNetwork);
		
		if(printWeights)
			System.out.print("\nFeatures:");
		
		for(int i = 0 ; i < network.getNumberOfOutputNeurons() ; i++) {
			NEATFeatureGene g = network.getFeatureGenes()[i]; 
			features[i] = g;
			if(printWeights)
				System.out.print(g.geneAsString()+" ");
		}
		if(printWeights)
			System.out.println();
		
	}

	@Override
	public void controlStep(double time) {
		neuralNetwork.controlStep(time);
		
		double[] out = neuralNetwork.getOutputNeuronStates();
		int maxIndex = 0;
		double maxVal = 0;
		
		for(int i = 0 ; i < out.length ; i++) {
			if(out[i] > maxVal) {
				maxIndex = i;
				maxVal = out[i];
			}
		}
			
		if(controllers[maxIndex] instanceof ParameterController) {
			ParameterController pc = (ParameterController)controllers[maxIndex];
			pc.setParameter(features[maxIndex].geneAsNumber().doubleValue());
		}
		
		controllers[maxIndex].controlStep(time);
		
	}
}