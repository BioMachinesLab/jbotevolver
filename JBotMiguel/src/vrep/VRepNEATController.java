package vrep;

import java.util.Vector;

import simulation.util.Arguments;
import evolutionaryrobotics.evolution.neat.NEATNeuralNetwork;
import evolutionaryrobotics.neuralnetworks.inputs.DummyNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SimpleNNOutput;

public class VRepNEATController extends VRepController {

    protected NEATNeuralNetwork ann;
    protected Vector<NNInput> inputs;

    public VRepNEATController(float[] parameters) {
        super(parameters);

        int index = 0;

        //read parameters, ignore first values
        int type = (int) parameters[index++];
        int nParams = (int) parameters[index++]; //locomotion parameters

        int inputNumber = (int) parameters[index++];
        int outputNumber = (int) parameters[index++];

        inputs = new Vector<NNInput>();
        Vector<NNOutput> outputs = new Vector<NNOutput>();

        for (int i = 0; i < inputNumber; i++) {
            inputs.add(new DummyNNInput(null));
        }

        outputs.add(new SimpleNNOutput(new Arguments("numberofoutputs=" + outputNumber)));
        
        //init NEAT ANN
        double[] annParams = ControllerFactory.extractWeights(parameters);
        ann = new NEATNeuralNetwork(inputs, outputs, new Arguments(""));
        ann.setWeights(annParams);
    }
    
    @Override
    public float[] controlStep(float[] inputs) {
        if(inputs == null || inputs.length == 0) {
            return null;
        }        
        for (int i = 0; i < inputs.length; i++) {
            ((DummyNNInput) this.inputs.get(i)).setInput(inputs[i]);
        }
        ann.controlStep(ticks);
        return ControllerFactory.doubleToFloat(ann.getOutputNeuronStates());
    }
}
