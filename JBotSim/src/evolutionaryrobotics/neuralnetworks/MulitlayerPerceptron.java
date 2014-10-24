package evolutionaryrobotics.neuralnetworks;

import java.util.Vector;

import net.jafama.FastMath;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;


public class MulitlayerPerceptron extends NeuralNetwork {
	@ArgumentsAnnotation(name="hiddennodes", defaultValue="5")
	int numberOfHiddenNodes    = 5;
	double[] hiddenNeuronStates;
	
	public MulitlayerPerceptron() {
	}
		
	public MulitlayerPerceptron(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments) {
		numberOfHiddenNodes = arguments.getArgumentIsDefined("hiddennodes") ? arguments.getArgumentAsInt("hiddennodes") : 5;
		create(inputs, outputs, numberOfHiddenNodes);
	}

	public void create(Vector<NNInput> inputs, Vector<NNOutput> outputs, int numberOfHiddenNeurons) {
		super.create(inputs, outputs);
		this.numberOfHiddenNodes = numberOfHiddenNeurons;
		setRequiredNumberOfWeights((getNumberOfInputNeurons() + 1) * numberOfHiddenNeurons  + (numberOfHiddenNeurons + 1) * getNumberOfOutputNeurons());

		hiddenNeuronStates = new double[numberOfHiddenNeurons];		
	}
	
	@Override
	protected double[] propagateInputs(double[] inputValues) {
	    for(int i = 0; i < numberOfHiddenNodes; i++) {
	        // Add the bias (weighted by the first weight to the i'th hidden node)
	        hiddenNeuronStates[i] = weights[i * (numberOfInputNeurons + 1)];			    
	        
	        for(int j = 0; j < numberOfInputNeurons; j++) {
	            // Compute the weight number
	            int ji = i * (numberOfInputNeurons + 1) + (j + 1);
	            // Add the weighted input
	            hiddenNeuronStates[i] += weights[ji] * inputValues[j];			 
	        }
	        
	        // Apply the transfer function (sigmoid with output in [0,1])
	        hiddenNeuronStates[i] = 1.0/( 1 + FastMath.expQuick(-hiddenNeuronStates[i]));	  
	    }

	    // Offset for the weights to the output nodes
	    int unBase = numberOfHiddenNodes * (numberOfInputNeurons + 1);

	    for( int i = 0; i < numberOfOutputNeurons; i++ ) {
	        // add the bias (wheigted by the first wheigt to the i^th output node
	        outputNeuronStates[i] = weights[unBase + i * (numberOfHiddenNodes + 1)];		 
	        
	        for( int j = 0; j < numberOfHiddenNodes; j++) 
	        {
	            // compute the weight number
	            int ji = unBase + i*(numberOfHiddenNodes+1) + (j + 1);
	            // add the weighted input
	            outputNeuronStates[i] += weights[ji] * hiddenNeuronStates[j];			
	        }        

	        outputNeuronStates[i] = 1.0/( 1 + FastMath.expQuick( - outputNeuronStates[i]) );	  
	    }
		
		return outputNeuronStates;
	}

	@Override
	public void reset() {
		// not necessary to reset a network without state
	}
}
