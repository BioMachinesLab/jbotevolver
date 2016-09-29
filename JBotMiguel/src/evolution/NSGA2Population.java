package evolution;

import java.util.Arrays;
import java.util.Comparator;

import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import multiobjective.MOChromosome;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.MuLambdaPopulation;

public class NSGA2Population extends MuLambdaPopulation{

	protected String mainObjective;
	
	public NSGA2Population(Arguments arguments) {
		super(arguments);
		mainObjective = arguments.getArgumentAsString("mainobjective");
	}
	
	@Override
	public void createRandomPopulation() {

		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		chromosomes = new MOChromosome[populationSize];
		
		if(fixedInitialPopulation) {
			for (int i = 0; i < populationSize; i++) {
				chromosomes[i] = new MOChromosome(initialWeights, i);
			}
			bestChromosome = chromosomes[0];
		} else {
			for (int i = 0; i < populationSize; i++) {
				double[] alleles = new double[genomelength];
				for (int j = 0; j < genomelength; j++) {
					alleles[j] = randomNumberGenerator.nextGaussian() * 2;
				}
				chromosomes[i] = new MOChromosome(alleles, i);
			}
		}

		resetGeneration();
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}
	
	@Override
	public void createNextGeneration() {
		
		randomNumberGenerator.setSeed(getGenerationRandomSeed());

        // Build archive P -- select the best half of each population
        Chromosome[] bestParents = new Chromosome[getPopulationSize()];
        for(int i = 0 ; i < bestParents.length ;i++) {
	        if (getNumberOfCurrentGeneration() > 0) {
	            // Sort the population individuals in descending order
	            Arrays.sort(chromosomes, new Comparator<Chromosome>() {
	                @Override
	                public int compare(Chromosome c1, Chromosome c2) {
	                    return Double.compare(c2.getFitness(), c1.getFitness());
	                }
	            });
	            // Reduce the population to the best half
	            setChromosomes(Arrays.copyOf(chromosomes, getPopulationSize()));
	        }
        
	        try {
		        // Copy this best half to the archive
		        for (int j = 0; j < bestParents.length; j++) {
		            bestParents[j] = chromosomes[j].clone();
		            bestParents[j].setId(j);
		        }
	        } catch(Exception e) {
	        	throw new RuntimeException(e.getMessage());
	        }
        }
        
        // Breed the populations (now composed only by the best half)
        Chromosome[] children = new Chromosome[getPopulationSize()];
        
        for(int i = 0 ; i < getPopulationSize() ; i++) {
	        Chromosome parent = tournamentSelection(bestParents,2);
	        Chromosome child = mutate(parent,i+getPopulationSize());
	        children[i] = child;
        }
        
        // Join the recently bred population with the best parents
        Chromosome[] inds = Arrays.copyOf(bestParents, getPopulationSize() * 2);
        System.arraycopy(children, 0, inds, getPopulationSize(), children.length);
        setChromosomes(inds);
        
        resetGeneration();
		currentGeneration++;

		// Create a new random seed for this generation
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}
	
	/**
	 * Requires the array to be sorted already!
	 */
	private Chromosome tournamentSelection(Chromosome[] cs, int n) {
		int bestIndex = cs.length;
		
		for(int i = 0 ; i < n ; i ++) { 
			int rand = randomNumberGenerator.nextInt(cs.length);
			if(rand < bestIndex)
				bestIndex = rand;
		}
		
		return cs[bestIndex];
	}
	
	private Chromosome mutate(Chromosome c, int id) {
		
		double[] alleles = new double[c.getAlleles().length];
		
		for (int j = 0; j < c.getAlleles().length; j++) {
			double allele = c.getAlleles()[j];
			if (randomNumberGenerator.nextDouble() < mutationRate) {
				allele = allele + randomNumberGenerator.nextGaussian();
				if (allele < -10)
					allele = -10;
				if (allele > 10)
					allele = 10;
			}
			alleles[j] = allele;
		}
		return new MOChromosome(alleles, id);
	}
	
	public void setEvaluationResultForId(int pos, EvaluationResult result) {
		setEvaluationResult(chromosomes[pos], result);
	}
	
	public void setEvaluationResult(Chromosome chromosome, EvaluationResult result) {
		MOChromosome moc = (MOChromosome)chromosome;
		moc.setEvaluationResult(result);
		
		double fitness = ((ExpandedFitness)result).getFitness();
			
		accumulatedFitness += fitness;

		if (fitness > bestFitness) {
			bestChromosome = chromosome;
			bestFitness = fitness;
		}

		if (fitness < worstFitness) {
			worstFitness = fitness;
		}
	}
	
	@Override
	public void setEvaluationResult(Chromosome chromosome, double fitness) {
		chromosome.setFitness(fitness);
		numberOfChromosomesEvaluated++;
	}

	@Override
	public void setEvaluationResultForId(int pos, double fitness) {
		if (pos >= chromosomes.length) {
			throw new java.lang.RuntimeException("No such position: " + pos
					+ " on the population");
		}
		setEvaluationResult(chromosomes[pos],fitness);
	}
	
}