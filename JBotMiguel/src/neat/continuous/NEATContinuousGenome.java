package neat.continuous;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.encog.EncogError;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATInnovation;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.util.Format;

public class NEATContinuousGenome extends NEATGenome implements Cloneable, Serializable {

	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of inputs.
	 */
	private int inputCount;

	/**
	 * The list that holds the links.
	 */
	private final List<NEATLinkGene> linksList = new ArrayList<NEATLinkGene>();

	/**
	 * THe network depth.
	 */
	private int networkDepth;

	/**
	 * The list that holds the neurons.
	 */
	private final List<NEATNeuronGene> neuronsList = new ArrayList<NEATNeuronGene>();

	/**
	 * The number of outputs.
	 */
	private int outputCount;

	/**
	 * Construct a genome by copying another.
	 * 
	 * @param other
	 *            The other genome.
	 */
	public NEATContinuousGenome(final NEATContinuousGenome other) {
		this.networkDepth = other.networkDepth;
		this.setPopulation(other.getPopulation());
		setScore(other.getScore());
		setAdjustedScore(other.getAdjustedScore());
		this.inputCount = other.inputCount;
		this.outputCount = other.outputCount;
		this.setSpecies(other.getSpecies());

		// copy neurons
		for (final NEATNeuronGene oldGene : other.getNeuronsChromosome()) {
			if(oldGene instanceof NEATContinuousNeuronGene) {
				final NEATNeuronGene newGene = new NEATContinuousNeuronGene((NEATContinuousNeuronGene)oldGene);
				this.neuronsList.add(newGene);
			} else {
				final NEATNeuronGene newGene = new NEATNeuronGene(oldGene);
				this.neuronsList.add(newGene);
			}
		}

		// copy links
		for (final NEATLinkGene oldGene : other.getLinksChromosome()) {
			final NEATLinkGene newGene = new NEATLinkGene(
					oldGene.getFromNeuronID(), oldGene.getToNeuronID(),
					oldGene.isEnabled(), oldGene.getInnovationId(),
					oldGene.getWeight());
			this.linksList.add(newGene);
		}

	}

	/**
	 * Create a NEAT gnome. Neuron genes will be added by reference, links will
	 * be copied.
	 * 
	 * @param neurons
	 *            The neurons to create.
	 * @param links
	 *            The links to create.
	 * @param inputCount
	 *            The input count.
	 * @param outputCount
	 *            The output count.
	 */
	public NEATContinuousGenome(final List<NEATNeuronGene> neurons,
			final List<NEATLinkGene> links, final int inputCount,
			final int outputCount) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;

		for (NEATLinkGene gene : links) {
			this.linksList.add(new NEATLinkGene(gene));
		}

		this.neuronsList.addAll(neurons);
	}

	/**
	 * Create a new genome with the specified connection density. This
	 * constructor is typically used to create the initial population.
	 * @param rnd Random number generator.
	 * @param pop The population.
	 * @param inputCount The input count.
	 * @param outputCount The output count.
	 * @param connectionDensity The connection density.
	 */
	public NEATContinuousGenome(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount,
			double connectionDensity) {
		initNoBootstrap(rnd, pop, inputCount, outputCount, connectionDensity);
	}
	
	public NEATContinuousGenome(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount, double connectionDensity, int bootstrap) {
		if(bootstrap == 0) {
			initNoBootstrap(rnd, pop, inputCount, outputCount, connectionDensity);
//		} else if(bootstrap == 1){
//			initBootstrap(rnd, pop, inputCount, outputCount, connectionDensity);
//		} else if(bootstrap == 2){
//			initBootstrap2(rnd, pop, inputCount, outputCount, connectionDensity);
		} else if(bootstrap == 3){
			initBootstrap3(rnd, pop, inputCount, outputCount, connectionDensity);
		}
	}
	
	protected void initNoBootstrap(Random rnd, NEATPopulation pop, int inputCount, int outputCount, double connectionDensity) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;

		// get the activation function
		ActivationFunction af = pop.getActivationFunctions().pickFirst();

		// first bias
		int innovationID = 0;
		NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias, af,
				inputCount, innovationID++);
		this.neuronsList.add(biasGene);

		// then inputs

		for (int i = 0; i < inputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Input, af,
					i, innovationID++);
			this.neuronsList.add(gene);
		}

		// then outputs

		for (int i = 0; i < outputCount; i++) {
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Output, af,
					i + inputCount + 1, innovationID++, 0, bias);
			this.neuronsList.add(gene);
		}

		// and now links
		for (int i = 0; i < inputCount + 1; i++) {
			for (int j = 0; j < outputCount; j++) {
				// make sure we have at least one connection
				if (this.linksList.size() < 1
						|| rnd.nextDouble() < connectionDensity) {
					long fromID = this.neuronsList.get(i).getId();
					long toID = this.neuronsList.get(inputCount + j + 1)
							.getId();
					double w = RangeRandomizer.randomize(rnd,
							-pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene gene = new NEATLinkGene(fromID, toID, true,
							innovationID++, w);
					this.linksList.add(gene);
				}
			}
		}
	}
	
	protected void initBootstrap(Random rnd, NEATPopulation pop, int inputCount, int outputCount, double connectionDensity) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;
		// get the activation function
		ActivationFunction af = pop.getActivationFunctions().pickFirst();

		//NEURONS
		
		// first bias
		int innovationID = 0;
		NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias, af,
				inputCount, innovationID++);
		this.neuronsList.add(biasGene);

		// then inputs
		for (int i = 0; i < inputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Input, af,
					i, innovationID++);
			this.neuronsList.add(gene);
		}
		
		// then outputs
		for (int i = 0; i < outputCount; i++) {
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Output, af,
					i + inputCount + 1, innovationID++, 0, bias);
			this.neuronsList.add(gene);
			
		}
		
		// then hidden
		int hiddenCount = 0;
		int maxHidden = Math.max(inputCount,outputCount);
		
		for (int i = 0 ; i < maxHidden ; i++) {
			if(rnd.nextDouble() < connectionDensity) {
				double decay = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				int id = i + inputCount + outputCount + 1;
				NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Hidden,
						af,id, innovationID++, decay, bias);
				this.neuronsList.add(gene);
				hiddenCount++;
			}
		}
		
		//CONNECTIONS
		
		//B > all
		
		int currentConnections = 0;
		
		for(int i = inputCount + 1 ; i < neuronsList.size() ; i++ ) {
			if(rnd.nextDouble() < connectionDensity) {
				long fromID = getBiasId();	
				long toID = this.neuronsList.get(i).getId();
				double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
				this.linksList.add(linkGene);
				currentConnections++;
			}
		}
		
		if(currentConnections == 0) {
			long bias = getBiasId();
			long chosenNeuron = neuronsList.get((int)((rnd.nextDouble()*(neuronsList.size() - 1 - inputCount) + 1 + inputCount))).getId();
			double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATLinkGene linkGene = new NEATLinkGene(bias, chosenNeuron, true, innovationID++, w);
			this.linksList.add(linkGene);
		}
		
		boolean[] inputsConnected = new boolean[inputCount];
		boolean[] hiddenConnected = new boolean[hiddenCount];
		boolean[] outputsConnected = new boolean[outputCount];
		
		//I > H
		if(hiddenCount > 0) {
			for(int i = 0 ; i < inputCount ; i++) {
				
				currentConnections = 0;
				long fromID = getInputId(i);
				
				for(int j = 0 ; j < hiddenCount ; j++) {				
					if(rnd.nextDouble() < connectionDensity) {
						
						long toID = getHiddenId(j);
						
						hiddenConnected[j] = true;
						inputsConnected[i] = true;
						
						double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
						NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
						this.linksList.add(linkGene);
						currentConnections++;
					}
				}

				if(currentConnections == 0) {
					int j = (int)(rnd.nextDouble()*hiddenCount);
					long toID = getHiddenId(j);
					
					inputsConnected[i] = true;
					hiddenConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
			
			//make sure everything has at least 1 connection!
			for(int j = 0 ; j < hiddenCount ; j++) {
				if(!hiddenConnected[j]) {
					int i = (int)(rnd.nextDouble()*inputCount);
					long fromID = getInputId(i);
					long toID = getHiddenId(j);
					
					inputsConnected[i] = true;
					hiddenConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
		}
		
		//H > H
		//not necessary to force inter-neuron connections in the hidden layer
		for(int i = 0 ; i < hiddenCount ; i++) {
			for(int j = 0 ; j < hiddenCount ; j++) {
				if(rnd.nextDouble() < connectionDensity) {
					long fromID = getHiddenId(i);
					long toID = getHiddenId(j);
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
		}
		
		//H > O
		if(hiddenCount > 0) {
			for(int i = 0 ; i < hiddenCount ; i++) {
				
				currentConnections = 0;
				long fromID = getHiddenId(i);
				
				for(int j = 0 ; j < outputCount ; j++) {
					if(rnd.nextDouble() < connectionDensity) {
						
						hiddenConnected[i] = true;
						outputsConnected[j] = true;
						
						long toID = getOutputId(j);
						double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
						NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
						this.linksList.add(linkGene);
						currentConnections++;
					}
				}

				if(currentConnections == 0) {
					int j = (int)(rnd.nextDouble()*outputCount);
					long toID = getOutputId(j);
					
					hiddenConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
			
			//make sure everything has at least 1 connection!
			for(int j = 0 ; j < outputCount ; j++) {
				if(!outputsConnected[j]) {
					int i = (int)(rnd.nextDouble()*hiddenCount);
					long fromID = getHiddenId(i);
					long toID = getOutputId(j);
					
					hiddenConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
		}
		
		//I > O
		if(hiddenCount == 0) {
			for (int j = 0; j < outputCount; j++) {
				
				long toID = getOutputId(j);
				currentConnections = 0;
				
				for (int i = 0; i < inputCount ; i++) {	
					if (rnd.nextDouble() < connectionDensity) {
						
						inputsConnected[i] = true;
						outputsConnected[j] = true;
						
						long fromID = getInputId(i);
						double w = RangeRandomizer.randomize(rnd,
								-pop.getWeightRange(), pop.getWeightRange());
						NEATLinkGene gene = new NEATLinkGene(fromID, toID, true,
								innovationID++, w);
						this.linksList.add(gene);
						currentConnections++;
					}
				}
				
				if(currentConnections == 0) {
					int i = (int)(rnd.nextDouble()*inputCount);
					long fromID = getInputId(i);
					
					inputsConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
			
			for(int i = 0 ; i < inputCount ; i++) {
				if(!inputsConnected[i]) {
					int j = (int)(rnd.nextDouble()*outputCount);
					long fromID = getInputId(i);
					long toID = getOutputId(j);
					
					inputsConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
					this.linksList.add(linkGene);
				}
			}
		}
	}
	
	protected void initBootstrap2(Random rnd, NEATPopulation pop, int inputCount, int outputCount, double connectionDensity) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;
		// get the activation function
		ActivationFunction af = pop.getActivationFunctions().pickFirst();

		//NEURONS
		
		// first bias
		int innovationID = 0;
		NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias, af,
				inputCount, innovationID++);
		this.neuronsList.add(biasGene);

		// then inputs
		for (int i = 0; i < inputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Input, af,
					i, innovationID++);
			this.neuronsList.add(gene);
		}
		
		// then outputs
		for (int i = 0; i < outputCount; i++) {
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Output, af,
					i + inputCount + 1, innovationID++,0,bias);
			this.neuronsList.add(gene);
			
		}
		
		// then hidden
		int hiddenCount = Math.max(inputCount,outputCount);
		
		for (int i = 0 ; i < hiddenCount ; i++) {
			double decay = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			int id = i + inputCount + outputCount + 1;
			NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Hidden,
					af,id, innovationID++, decay, bias);
			this.neuronsList.add(gene);
		}
		
		//CONNECTIONS
		
		//B > all
		
		for(int i = inputCount + 1 ; i < neuronsList.size() ; i++ ) {
			long fromID = getBiasId();	
			long toID = this.neuronsList.get(i).getId();
			double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
			this.linksList.add(linkGene);
		}
		
		//I > H
		for(int i = 0 ; i < inputCount ; i++) {
			
			long fromID = getInputId(i);
			
			for(int j = 0 ; j < hiddenCount ; j++) {				
				long toID = getHiddenId(j);
				double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
				this.linksList.add(linkGene);
			}
		}
		
		//H > H
		for(int i = 0 ; i < hiddenCount ; i++) {
			
			long fromID = getHiddenId(i);
			
			for(int j = 0 ; j < hiddenCount ; j++) {
				long toID = getHiddenId(j);
				double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
				this.linksList.add(linkGene);
			}
		}
		
		//H > O
		for(int i = 0 ; i < hiddenCount ; i++) {
			
			long fromID = getHiddenId(i);
			
			for(int j = 0 ; j < outputCount ; j++) {
				long toID = getOutputId(j);
				double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innovationID++, w);
				this.linksList.add(linkGene);
			}
		}
		
		//I > O
		for (int i = 0; i < inputCount ; i++) {
			
			long fromID = getInputId(i);
			
			for (int j = 0; j < outputCount; j++) {
				if (rnd.nextDouble() < connectionDensity) {
					long toID = getOutputId(j);
					double w = RangeRandomizer.randomize(rnd,
							-pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene gene = new NEATLinkGene(fromID, toID, true,
							innovationID++, w);
					this.linksList.add(gene);
				}
			}
		}
	}
	
	protected void initBootstrap3(Random rnd, NEATPopulation pop, int inputCount, int outputCount, double connectionDensity) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;
		// get the activation function
		ActivationFunction af = pop.getActivationFunctions().pickFirst();

		//NEURONS
		
		// first bias
		int innovationID = 0;
		
		NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias, af,
				inputCount, innovationID++);
		this.neuronsList.add(biasGene);

		// then inputs
		for (int i = 0; i < inputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Input, af,
					i, innovationID++);
			this.neuronsList.add(gene);
		}
		
		// then outputs
		for (int i = 0; i < outputCount; i++) {
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Output, af,
					i + inputCount + 1, innovationID++,0,bias);
			this.neuronsList.add(gene);
			
		}
		
		// then hidden
		//TODO
		int hiddenCount = (int)connectionDensity;
		
		for (int i = 0 ; i < hiddenCount ; i++) {
			double decay = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			long id = pop.assignGeneID();
			NEATInnovation innov = pop.getInnovations().findInnovation(id);
			NEATContinuousNeuronGene gene = new NEATContinuousNeuronGene(NEATNeuronType.Hidden,
					af,id, innov.getInnovationID(), decay, bias);
			this.neuronsList.add(gene);
		}
		
		//TODO hardcoded!
		connectionDensity = 0.5;
		
		//CONNECTIONS
		
		//B > all
		
		int currentConnections = 0;
		
		for(int i = inputCount + 1 ; i < neuronsList.size() ; i++ ) {
			if(rnd.nextDouble() < connectionDensity) {
				long fromID = getBiasId();
				long toID = this.neuronsList.get(i).getId();
				double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
				NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
				NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
				this.linksList.add(linkGene);
				currentConnections++;
			}
		}
		
		if(currentConnections == 0) {
			long bias = getBiasId();
			long chosenNeuron = neuronsList.get((int)((rnd.nextDouble()*(neuronsList.size() - 1 - inputCount) + 1 + inputCount))).getId();
			double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			NEATInnovation innov = pop.getInnovations().findInnovation(bias, chosenNeuron);
			NEATLinkGene linkGene = new NEATLinkGene(bias, chosenNeuron, true, innov.getInnovationID(), w);
			this.linksList.add(linkGene);
		}
		
		boolean[] inputsConnected = new boolean[inputCount];
		boolean[] hiddenConnected = new boolean[hiddenCount];
		boolean[] outputsConnected = new boolean[outputCount];
		
		//I > H
		if(hiddenCount > 0) {
			for(int i = 0 ; i < inputCount ; i++) {
				
				currentConnections = 0;
				long fromID = getInputId(i);
				
				for(int j = 0 ; j < hiddenCount ; j++) {				
					if(rnd.nextDouble() < connectionDensity) {
						
						long toID = getHiddenId(j);
						
						hiddenConnected[j] = true;
						inputsConnected[i] = true;
						
						double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
						NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
						NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
						this.linksList.add(linkGene);
						currentConnections++;
					}
				}

				if(currentConnections == 0) {
					int j = (int)(rnd.nextDouble()*hiddenCount);
					long toID = getHiddenId(j);
					
					inputsConnected[i] = true;
					hiddenConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
			
			//make sure everything has at least 1 connection!
			for(int j = 0 ; j < hiddenCount ; j++) {
				if(!hiddenConnected[j]) {
					int i = (int)(rnd.nextDouble()*inputCount);
					long fromID = getInputId(i);
					long toID = getHiddenId(j);
					
					inputsConnected[i] = true;
					hiddenConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
		}
		
		//H > H
		//not necessary to force inter-neuron connections in the hidden layer
		for(int i = 0 ; i < hiddenCount ; i++) {
			for(int j = 0 ; j < hiddenCount ; j++) {
				if(rnd.nextDouble() < connectionDensity) {
					long fromID = getHiddenId(i);
					long toID = getHiddenId(j);
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
		}
		
		//H > O
		if(hiddenCount > 0) {
			for(int i = 0 ; i < hiddenCount ; i++) {
				
				currentConnections = 0;
				long fromID = getHiddenId(i);
				
				for(int j = 0 ; j < outputCount ; j++) {
					if(rnd.nextDouble() < connectionDensity) {
						
						hiddenConnected[i] = true;
						outputsConnected[j] = true;
						
						long toID = getOutputId(j);
						double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
						NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
						NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
						this.linksList.add(linkGene);
						currentConnections++;
					}
				}

				if(currentConnections == 0) {
					int j = (int)(rnd.nextDouble()*outputCount);
					long toID = getOutputId(j);
					
					hiddenConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
			
			//make sure everything has at least 1 connection!
			for(int j = 0 ; j < outputCount ; j++) {
				if(!outputsConnected[j]) {
					int i = (int)(rnd.nextDouble()*hiddenCount);
					long fromID = getHiddenId(i);
					long toID = getOutputId(j);
					
					hiddenConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
		}
		
		//I > O
		if(hiddenCount == 0) {
			for (int j = 0; j < outputCount; j++) {
				
				long toID = getOutputId(j);
				currentConnections = 0;
				
				for (int i = 0; i < inputCount ; i++) {	
					if (rnd.nextDouble() < connectionDensity) {
						
						inputsConnected[i] = true;
						outputsConnected[j] = true;
						
						long fromID = getInputId(i);
						double w = RangeRandomizer.randomize(rnd,
								-pop.getWeightRange(), pop.getWeightRange());
						NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
						NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
						this.linksList.add(linkGene);
						currentConnections++;
					}
				}
				
				if(currentConnections == 0) {
					int i = (int)(rnd.nextDouble()*inputCount);
					long fromID = getInputId(i);
					
					inputsConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
			
			for(int i = 0 ; i < inputCount ; i++) {
				if(!inputsConnected[i]) {
					int j = (int)(rnd.nextDouble()*outputCount);
					long fromID = getInputId(i);
					long toID = getOutputId(j);
					
					inputsConnected[i] = true;
					outputsConnected[j] = true;
					
					double w = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
					NEATInnovation innov = pop.getInnovations().findInnovation(fromID, toID);
					NEATLinkGene linkGene = new NEATLinkGene(fromID, toID, true, innov.getInnovationID(), w);
					this.linksList.add(linkGene);
				}
			}
		}
	}
	
	private long getBiasId() {
		return neuronsList.get(0).getId();
	}
	
	private long getInputId(int index) {
		return this.neuronsList.get(index + 1).getId();
	}
	
	private long getOutputId(int index) {
		return this.neuronsList.get(index + inputCount + 1).getId();
	}
	
	private long getHiddenId(int index) {
		return this.neuronsList.get(index + inputCount + outputCount + 1).getId();
	}
	
	public NEATContinuousGenome() {}

	/**
	 * @return The number of input neurons.
	 */
	public int getInputCount() {
		return this.inputCount;
	}

	/**
	 * @return The network depth.
	 */
	public int getNetworkDepth() {
		return this.networkDepth;
	}

	/**
	 * @return The number of genes in the links chromosome.
	 */
	public int getNumGenes() {
		return this.linksList.size();
	}

	/**
	 * @return The output count.
	 */
	public int getOutputCount() {
		return this.outputCount;
	}

	/**
	 * @param networkDepth
	 *            the networkDepth to set
	 */
	public void setNetworkDepth(final int networkDepth) {
		this.networkDepth = networkDepth;
	}

	/**
	 * Sort the genes.
	 */
	public void sortGenes() {
		Collections.sort(this.linksList);
	}

	/**
	 * @return the linksChromosome
	 */
	public List<NEATLinkGene> getLinksChromosome() {
		return this.linksList;
	}

	/**
	 * @return the neuronsChromosome
	 */
	public List<NEATNeuronGene> getNeuronsChromosome() {
		return this.neuronsList;
	}

	/**
	 * @param inputCount
	 *            the inputCount to set
	 */
	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}

	/**
	 * @param outputCount
	 *            the outputCount to set
	 */
	public void setOutputCount(int outputCount) {
		this.outputCount = outputCount;
	}

	/**
	 * Validate the structure of this genome.
	 */
	public void validate() {

		// make sure that the bias neuron is where it should be
		NEATNeuronGene g = this.neuronsList.get(0);
		if (g.getNeuronType() != NEATNeuronType.Bias) {
			throw new EncogError("NEAT Neuron Gene 0 should be a bias gene.");
		}

		// make sure all input neurons are at the beginning
		for (int i = 1; i <= this.inputCount; i++) {
			NEATNeuronGene gene = this.neuronsList.get(i);
			if (gene.getNeuronType() != NEATNeuronType.Input) {
				throw new EncogError("NEAT Neuron Gene " + i
						+ " should be an input gene.");
			}
		}

		// make sure that there are no double links
		Map<String, NEATLinkGene> map = new HashMap<String, NEATLinkGene>();
		for (NEATLinkGene nlg : this.linksList) {
			String key = nlg.getFromNeuronID() + "->" + nlg.getToNeuronID();
			if (map.containsKey(key)) {
				throw new EncogError("Double link found: " + key);
			}
			map.put(key, nlg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(Genome source) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Find the neuron with the specified nodeID.
	 * 
	 * @param nodeID
	 *            The nodeID to look for.
	 * @return The neuron, if found, otherwise null.
	 */
	public NEATNeuronGene findNeuron(long nodeID) {
		for (NEATNeuronGene gene : this.neuronsList) {
			if (gene.getId() == nodeID)
				return gene;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(this.getClass().getSimpleName());
		result.append(",score=");
		result.append(Format.formatDouble(this.getScore(), 2));
		result.append(",adjusted score=");
		result.append(Format.formatDouble(this.getAdjustedScore(), 2));
		result.append(",birth generation=");
		result.append(this.getBirthGeneration());
		result.append(",neurons=");
		result.append(this.neuronsList.size());
		result.append(",links=");
		result.append(this.linksList.size());
		result.append("]");
		return result.toString();
	}
}
