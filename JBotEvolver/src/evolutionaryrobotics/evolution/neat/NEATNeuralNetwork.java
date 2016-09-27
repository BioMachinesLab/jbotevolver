package evolutionaryrobotics.evolution.neat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import simulation.util.Arguments;
import evolutionaryrobotics.evolution.neat.core.NEATChromosome;
import evolutionaryrobotics.evolution.neat.core.NEATFeatureGene;
import evolutionaryrobotics.evolution.neat.core.NEATLinkGene;
import evolutionaryrobotics.evolution.neat.core.NEATNetDescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATNeuralNet;
import evolutionaryrobotics.evolution.neat.core.NEATNodeGene;
import evolutionaryrobotics.evolution.neat.data.core.NetworkInput;
import evolutionaryrobotics.evolution.neat.data.core.NetworkOutputSet;
import evolutionaryrobotics.evolution.neat.data.csv.CSVInput;
import evolutionaryrobotics.evolution.neat.ga.core.Gene;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class NEATNeuralNetwork extends NeuralNetwork{

	public static final double NODE = 0d, LINK = 1d, FEATURE = 2d;
	protected NEATNeuralNet network;
	
	public NEATNeuralNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments) {
		create(inputs, outputs);
		if(arguments.getArgumentIsDefined("weights")) {
			String net = arguments.getArgumentAsString("weights");
			network = deserialize(net);
		}
	}

	@Override
	protected double[] propagateInputs(double[] inputValues) {
		double[] vals = Arrays.copyOf(inputValues, inputValues.length);
		for (int i = 0; i < vals.length; i++) {
			vals[i] = vals[i] * 2 - 1;
		}
		NetworkInput in = new CSVInput(vals);
		NetworkOutputSet output = network.execute(in);
		return output.nextOutput().values();
	}

	@Override
	public void setWeights(double[] weights) {
		StringBuilder builder = new StringBuilder();
		for(double d : weights) {
			builder.append(d);
			builder.append(',');
		}
		this.network = deserialize(builder.toString());
	}
	
	@Override
	public void reset() {
        NEATNeuralNet newNet = new NEATNeuralNet();
        newNet.createNetStructure(network.netDescriptor());
        newNet.updateNetStructure();
        this.network = newNet;
    }
	
	public NEATFeatureGene[] getFeatureGenes() {
		return network.getFeatureGenes();
	}

    public static NEATNeuralNet deserialize(String ser) {
        String[] split = ser.split(",");
        ArrayList<Double> stuff = new ArrayList<Double>();
        for (String s : split) {
            stuff.add(Double.parseDouble(s));
        }

        ArrayList<Gene> genes = new ArrayList<Gene>();
        Iterator<Double> iter = stuff.iterator();
        while (iter.hasNext()) {
            double type = iter.next();
            if (type == NODE) {
                int id = (int) (double) iter.next();
                double sigF = iter.next();
                int t = (int) (double) iter.next();
                double bias = iter.next();
                genes.add(new NEATNodeGene(0, id, sigF, t, bias));
            } else if (type == LINK) {
                boolean enabled = iter.next() == 1d;
                int from = (int) (double) iter.next();
                int to = (int) (double) iter.next();
                double weight = iter.next();
                genes.add(new NEATLinkGene(0, enabled, from, to, weight));
            } else if(type == FEATURE) {
                double weight = iter.next();
                int innov = iter.next().intValue();
                genes.add(new NEATFeatureGene(innov, weight));
            }

        }
        Gene[] geneArray = new Gene[genes.size()];
        genes.toArray(geneArray);
        NEATChromosome chromo = new NEATChromosome(geneArray);
        NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
        descr.updateStructure(chromo);
        NEATNeuralNet network = new NEATNeuralNet();
        network.createNetStructure(descr);
        network.updateNetStructure();
        return network;
    }
}