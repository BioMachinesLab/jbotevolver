package neatCompatibilityImplementation;

import java.util.Vector;

import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.util.EngineArray;

import simulation.util.Arguments;

import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;


public class ERNEATNetwork extends NeuralNetwork {

	private static final long serialVersionUID = 1L;
	protected org.encog.neural.neat.NEATNetwork network;
	//protected MLData inputs;

	public ERNEATNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		this.create(inputs, outputs);
	}

	@Override
	public void create(Vector<NNInput> inputs, Vector<NNOutput> outputs) {
		super.create(inputs, outputs);
		//this.inputs = new BasicMLData(super.inputNeuronStates);
		//System.out.println("CREATED");
		//setupNetwork();
	}

	@Override
	protected double[] propagateInputs(double[] inputValues) {
		//inputs.setData(inputValues);
		//return network.compute(inputs).getData();

		return this.processInputs(inputValues);
	}

	public double[] processInputs(double[] input) {
		double[] result = new double[network.getOutputCount()];

		double[] preActivation = network.getPreActivation();
		preActivation[0] = 1.0;
		double[] postActivation = network.getPostActivation();
		NEATLink[] links = network.getLinks();
		// copy input
		EngineArray.arrayCopy(input, 0, postActivation, 1, network.getInputCount());

		// 1 activatio cycles
		//for(int i = 0; i < network.getActivationCycles(); i++){
		for (int j = 0; j < links.length; j++) {
			preActivation[links[j].getToNeuron()] += postActivation[links[j].getFromNeuron()] * links[j].getWeight();
		}
		//}
		for (int j = network.getOutputIndex(); j < preActivation.length; j++) {
			postActivation[j] = preActivation[j];
			network.getActivationFunctions()[j].activationFunction(postActivation, j, 1);
			preActivation[j] = 0.0F;
		}

		// copy output
		EngineArray.arrayCopy(postActivation, network.getOutputIndex(), result, 0, network.getOutputCount());

		return result;
	}

	@Override
	public void reset() {
		EngineArray.fill(network.getPreActivation(), 0.0);
		EngineArray.fill(network.getPostActivation(), 0.0);
		network.getPostActivation()[0] = 1.0;
	}

	public NEATNetwork getNEATNetwork() {
		return network;
	}

	public void setNEATNetwork(NEATNetwork newNetwork) {
		//System.out.println("SETTING NET");
		this.network = newNetwork;
	}

	public void controlStep(double time) {
		super.controlStep(time);
	}
}
