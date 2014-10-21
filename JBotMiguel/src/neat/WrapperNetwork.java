package neat;

import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.GenomeFactory;

import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

public abstract class WrapperNetwork extends NeuralNetwork{
	
	private static final long serialVersionUID = 6188183493087410730L;

	public abstract void setNetwork(MLMethod network);
	
	public abstract GeneticCODEC getCODEC();
	
	public abstract GenomeFactory getGenomeFactory();
		
}
