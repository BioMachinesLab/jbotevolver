package neat.parameters;

import java.util.ArrayList;
import java.util.List;
import neat.continuous.NEATContinuousNeuronGene;
import neat.layerered.ANNNeuron;
import neat.layerered.continuous.ANNNeuronContinuous;
import neat.layerered.continuous.LayeredContinuousNEATCODEC;
import org.encog.neural.neat.training.NEATNeuronGene;

public class ParameterNEATCODEC extends LayeredContinuousNEATCODEC {

	@Override
	protected ArrayList<ANNNeuron> createNeurons(List<NEATNeuronGene> neuronsGenome) throws Exception {
		ArrayList<ANNNeuron> standardNeurons = new ArrayList<ANNNeuron>(neuronsGenome.size());
		for(NEATNeuronGene neuronGene : neuronsGenome){
			int newNeuronType;
			switch(neuronGene.getNeuronType()){
			case Bias:
				newNeuronType = ANNNeuron.BIAS_NEURON;
				break;
			case Input:
				newNeuronType = ANNNeuron.INPUT_NEURON;
				break;
			case Output:
				newNeuronType = ANNNeuron.OUTPUT_NEURON;
				break;
			case Hidden:
				newNeuronType = ANNNeuron.HIDDEN_NEURON;
				break;
			default:
				throw new Exception("unexpected neuron type");
			}
			
			ANNNeuron newNeuron;
			if(newNeuronType == ANNNeuron.OUTPUT_NEURON) {
				ParameterNeuronGene continuousGene = (ParameterNeuronGene)neuronGene;
				newNeuron = new ParameterNeuron(continuousGene.getId(), newNeuronType, continuousGene.getActivationFunction(),continuousGene.getBias(), continuousGene.getParameter());
			} else if(neuronGene instanceof NEATContinuousNeuronGene) {
				NEATContinuousNeuronGene continuousGene = (NEATContinuousNeuronGene)neuronGene;
				newNeuron = new ANNNeuronContinuous(neuronGene.getId(), newNeuronType, neuronGene.getActivationFunction(),continuousGene.getDecay(),continuousGene.getBias());
			} else {
				//id, not innovationId.
				//bias id = input count, see neat genome for details
				newNeuron = new ANNNeuron(neuronGene.getId(), newNeuronType, neuronGene.getActivationFunction());
			}
			
			standardNeurons.add(newNeuron);
		}
		return standardNeurons;
	}
}