package neat.evolution;

import java.io.Serializable;
import java.util.Random;
import neat.WrapperNetwork;
import org.encog.ml.MLError;
import org.encog.ml.MLRegression;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.species.BasicSpecies;
import org.encog.neural.hyperneat.FactorHyperNEATGenome;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATInnovationList;

/**
 * This class is necessary because the way the networks are initialized
 * breaks parallel evaluation of networks. They cannot all share the same
 * random number generator.
 * 
 * @author miguelduarte42
 */
public class NEATEncogPopulation extends NEATPopulation implements Serializable, MLError, MLRegression {
	
	private static final long serialVersionUID = -4688694377130370508L;
	private WrapperNetwork network;
	private int bootstrap;

	public NEATEncogPopulation() {

	}

	public NEATEncogPopulation(final int inputCount, final int outputCount, final int populationSize) {
		super(inputCount, outputCount, populationSize);
	}
	public NEATEncogPopulation(final int inputCount, final int outputCount, final int populationSize, WrapperNetwork network, int bootstrap) {
		this(inputCount, outputCount, populationSize);
		this.network = network;
		this.bootstrap = bootstrap;
	}

	public NEATEncogPopulation(final Substrate theSubstrate, final int populationSize) {
		super(theSubstrate,populationSize);
	}

	public void reset() {
		// create the genome factory
		if (isHyperNEAT()) {
			setCODEC(new HyperNEATCODEC());
			setGenomeFactory(new FactorHyperNEATGenome());
		} else {
			setCODEC(network.getCODEC());
			setGenomeFactory(network.getGenomeFactory());
		}
		
		// create the new genomes
		getSpecies().clear();

		// reset counters
		getGeneIDGenerate().setCurrentID(1);
		getInnovationIDGenerate().setCurrentID(1);

		final Random rnd = getRandomNumberFactory().factor();

		// create the initial population
		
		if(bootstrap > 0) {
			
			if(bootstrap == 3) {
				int nHidden = 11;
				
				//Innovations List manual init
				NEATInnovationList innovationList = new NEATInnovationList();
				innovationList.setPopulation(this);
				setInnovations(innovationList);
				
				//bias
				innovationList.findInnovation(assignGeneID());
				
				// input neurons
				for (int i = 0; i < getInputCount(); i++) {
					innovationList.findInnovation(assignGeneID());
				}

				// output neurons
				for (int i = 0; i < getOutputCount(); i++) {
					innovationList.findInnovation(assignGeneID());
				}
				
				for (int i = 0; i <= nHidden; i++) {
					
					// create one default species
					final BasicSpecies species = new BasicSpecies();
					species.setPopulation(this);
					
					int numberofChildren = getPopulationSize()/(nHidden+1);
					
					final Genome genome = getGenomeFactory().factor(new Random(rnd.nextLong()), this, getInputCount(), getOutputCount(), i);
					
					for(int j = 0 ; j < numberofChildren ; j++) {
						species.add(getGenomeFactory().factor(genome));
					}
					
					species.setLeader(species.getMembers().get(0));
					getSpecies().add(species);
				}
				
				/*	} else {
				 //TODO careful because here the innovations are not set...
				// create one default species
				final BasicSpecies defaultSpecies = new BasicSpecies();
				defaultSpecies.setPopulation(this);
				
				for (int i = 0; i < getPopulationSize(); i++) {
					double density = 1.0/getPopulationSize()*i;
					final Genome genome = getGenomeFactory().factor(new Random(rnd.nextLong()), this,
							getInputCount(), getOutputCount(),
							density);
					defaultSpecies.add(genome);
				}
				 
				defaultSpecies.setLeader(defaultSpecies.getMembers().get(0));
				getSpecies().add(defaultSpecies);
*/
			}
		} else {
			// create one default species
			final BasicSpecies defaultSpecies = new BasicSpecies();
			defaultSpecies.setPopulation(this);
			
			for (int i = 0; i < getPopulationSize(); i++) {
				final Genome genome = getGenomeFactory().factor(new Random(rnd.nextLong()), this,
						getInputCount(), getOutputCount(),
						getInitialConnectionDensity());
				defaultSpecies.add(genome);
			}
			defaultSpecies.setLeader(defaultSpecies.getMembers().get(0));
			getSpecies().add(defaultSpecies);
			
			// create initial innovations
			setInnovations(new NEATInnovationList(this));
		}
	}
}