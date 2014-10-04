package neat.layerered;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.genetic.GeneticError;
import org.encog.neural.NeuralNetworkError;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

public class LayeredNEATCODEC implements GeneticCODEC, Serializable {

	@Override
	public MLMethod decode(Genome genome) {
		final NEATGenome neatGenome = (NEATGenome) genome;
		//final NEATPopulation pop = (NEATPopulation) neatGenome.getPopulation();
		final List<NEATNeuronGene> neuronsGenome = neatGenome
				.getNeuronsChromosome();
		final List<NEATLinkGene> linksGenome = neatGenome
				.getLinksChromosome();

		if (neuronsGenome.get(0).getNeuronType() != NEATNeuronType.Bias) {
			throw new NeuralNetworkError(
					"The first neuron must be the bias neuron, this genome is invalid.");
		}

		//create synapses
		ArrayList<ANNSynapse> synapses = this.createSynapses(linksGenome);
		ArrayList<ANNNeuron> neurons = null;
		try {
			neurons = this.createNeurons(neuronsGenome);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LayeredANN net = createNetFromStructure(synapses, neurons);
		
		return net;
		
	}
	
	protected ArrayList<ANNNeuron> getCopyNeurons(ArrayList<ANNNeuron> original) {
		ArrayList<ANNNeuron> neurons = new ArrayList<ANNNeuron>();
		for(ANNNeuron n : original)
			neurons.add(n.shallowCopy());
		return neurons;
	}
	
	protected ArrayList<ANNSynapse> getCopySynapses(ArrayList<ANNSynapse> original) {
		ArrayList<ANNSynapse> synapses = new ArrayList<ANNSynapse>();
		for(ANNSynapse s : original)
			synapses.add(s.copy());
		return synapses;
	}

	public LayeredANN createNetFromStructure(ArrayList<ANNSynapse> s,
			ArrayList<ANNNeuron> n) {
		ArrayList<ANNSynapse> synapses = getCopySynapses(s);
		ArrayList<ANNNeuron> neurons = getCopyNeurons(n);
		
		HashMap<Long, ANNNeuron> idToNeuron = createNeuronsMap(neurons);
		//assign connections to neurons
		this.assignConnectionsToNeurons(synapses, idToNeuron);
		//assign neuron depth
		this.assignNeuronDepth(this.getOutputNeurons(neurons), 0);

		//normalise and sort depth to create layered net(ascending depth)
		ArrayList<Integer> layerIds = getLayersIds(neurons);
		Collections.sort(layerIds);

		ANNLayer[] layers = computeLayers(neurons, layerIds);
		/*System.out.println("L: " + layers.length + 
				"; l1: " + layers[0].getNeurons().size() + 
				"; l2: " + layers[1].getNeurons().size());*/
		
		if(layers[layers.length-1].isHiddenLayer()) {
			throw new NeuralNetworkError("Last layer is not output layer!");
		}
		
		return new LayeredANN(layers);
	}

	public ANNLayer[] computeLayers(ArrayList<ANNNeuron> neurons, 
			ArrayList<Integer> layerIds){
		ANNLayer[] layers = new ANNLayer[layerIds.size()];
		for(int i = 0; i < layerIds.size(); i++){
			int idIndex = layerIds.size() - 1 - i;
			int currentLayerId = layerIds.get(idIndex);
			ArrayList<ANNNeuron> layerNeurons = 
					getNeuronsWithDepth(currentLayerId, neurons);
			Collections.sort(layerNeurons);
			layers[i] = new ANNLayer(currentLayerId, layerNeurons);
		}
		return layers;
	}

	protected ArrayList<ANNNeuron> getNeuronsWithDepth(int depth,
			ArrayList<ANNNeuron> allNeurons) {
		ArrayList<ANNNeuron> layerNeurons = new ArrayList<ANNNeuron>();
		for(ANNNeuron n : allNeurons){
			if(n.getNeuronDepth() == depth)
				layerNeurons.add(n);
		}
		//System.out.println("n: " + layerNeurons.size() + "; " + depth);
		return layerNeurons;
	}

	protected ArrayList<Integer> getLayersIds(ArrayList<ANNNeuron> neurons) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(ANNNeuron n : neurons){
			int depth = n.getNeuronDepth();
			//disconnected pieces? may happen.
			if(depth == -1){
				if(n.getType() == ANNNeuron.INPUT_NEURON){
					depth = Integer.MAX_VALUE;
				}
				else if(n.getType() == ANNNeuron.BIAS_NEURON){
					depth = Integer.MAX_VALUE;
				}
				//disconnected piece? convention = 2nd layer
				else if(n.getIncomingConnections().isEmpty()  || (n.getIncomingNeurons().size() == 1 && n.getIncomingNeurons().get(0).getId() == n.getId())){
					depth = Integer.MAX_VALUE - 1;
					n.setNeuronDepth(depth);
				}
				else {
					for(ANNNeuron incoming : n.getIncomingNeurons()){
						depth = Math.max(depth, incoming.getNeuronDepth());
					}
				}
			}
			
			if(depth == Integer.MAX_VALUE && n.getType() == ANNNeuron.HIDDEN_NEURON) {
				throw new NeuralNetworkError("Hidden neuron in the input layer!");
			}
			
			if(depth != -1 && !ids.contains(depth))
				ids.add(depth);
		}
		return ids;

	}

	public void assignNeuronDepth(ArrayList<ANNNeuron> neurons, int depth) {
		for (int i = 0; i < neurons.size(); i++) {
			ANNNeuron neuron = neurons.get(i);
			
			if (neuron.getType() == ANNNeuron.OUTPUT_NEURON) {
				if (neuron.getNeuronDepth() == -1) {
					neuron.setNeuronDepth(0);
					this.assignNeuronDepth(
							neuron.getIncomingNeurons(), depth + 1);
				}
			} else if (neuron.getType() == ANNNeuron.HIDDEN_NEURON){
				if (neuron.getNeuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.getIncomingNeurons(), 
							depth + 1);				
				}
			} else if (neuron.getType() == ANNNeuron.INPUT_NEURON
					|| neuron.getType() == ANNNeuron.BIAS_NEURON) {
				neuron.setNeuronDepth(Integer.MAX_VALUE);
			} 
		}
	}

	protected ArrayList<ANNNeuron> getOutputNeurons(ArrayList<ANNNeuron> allNeurons){
		ArrayList<ANNNeuron> outputNeurons = new ArrayList<ANNNeuron>();
		for(ANNNeuron n : allNeurons){
			if(n.getType() == ANNNeuron.OUTPUT_NEURON){
				outputNeurons.add(n);
			}
		}
		return outputNeurons;
	}

	protected void assignConnectionsToNeurons(ArrayList<ANNSynapse> synapses,
			HashMap<Long, ANNNeuron> idToNeuron) {
		for(ANNSynapse s : synapses){

			long to = s.getToNeuron();
			ANNNeuron toNeuron = idToNeuron.get(to);

			//ADD INCOMING SYNAPSE
			toNeuron.addIncomingSynapse(s);
			//ADD INCOMING NEURON
			ANNNeuron n = idToNeuron.get(s.getFromNeuron());
			toNeuron.addIncomingNeuron(n);
		}
	}

	protected HashMap<Long, ANNNeuron> createNeuronsMap(
			ArrayList<ANNNeuron> neurons) {

		HashMap<Long,ANNNeuron> map = new HashMap<Long, ANNNeuron>();

		for(ANNNeuron n : neurons){
			map.put(n.getId(), n);
		}

		return map;
	}

	protected ArrayList<ANNNeuron> createNeurons(
			List<NEATNeuronGene> neuronsGenome) throws Exception {
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
			//id, not innovationId.
			//bias id = input count, see neat genome for details
			ANNNeuron newNeuron = new ANNNeuron(neuronGene.getId(), 
					newNeuronType,
					neuronGene.getActivationFunction());
			standardNeurons.add(newNeuron);
		}
		return standardNeurons;
	}

	protected ArrayList<ANNSynapse> createSynapses(List<NEATLinkGene> linksGenome) {
		ArrayList<ANNSynapse> synapses = new ArrayList<ANNSynapse>(linksGenome.size());
		for(NEATLinkGene link : linksGenome){
			if(link.isEnabled()){
				ANNSynapse newSynapse = new ANNSynapse(
						link.getInnovationId(), 
						link.getWeight(), link.getFromNeuronID(), 
						link.getToNeuronID());
				synapses.add(newSynapse);
			}
		}
		return synapses;
	}

	@Override
	public Genome encode(MLMethod phenotype) {
		throw new GeneticError(
				"Encoding of a NEAT network is not supported.");
	}


}
