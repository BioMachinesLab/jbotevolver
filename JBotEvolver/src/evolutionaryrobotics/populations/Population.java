package evolutionaryrobotics.populations;

import java.io.Serializable;
import java.util.Random;

import evolutionaryrobotics.neuralnetworks.Chromosome;

import simulation.Simulator;
import simulation.util.Arguments;


/**
 * Super-class for populations/evolutionary algorithms.
 * 
 * @author alc
 */

public abstract class Population implements Serializable {
	protected int generationRandomSeed;
	protected int numberOfSamplesPerChromosome;
	protected int numberOfGenerations    = 100;

	protected int numberOfStepsPerSample    = 1000;

	protected boolean enableIncreasingSamplesPerChromosome = false;
	protected int     increaseStartSamplesPerChromosome;
	protected int     increaseMaxSamplesPerChromosome;      
	protected double  increaseAdditionalStepsPerSample;
	
	protected boolean enableIncreasingStepsPerSample          = false;
	protected int     increaseStartStepsPerSample;
	protected int     increaseMaxStepsPerSample;
	protected double  increaseAdditionalSamplesPerGeneration;
	protected double  mutationRate;//                            = 0.05;
	
	protected double  fitnessThreshold                                 = 0;
	protected boolean enableFitnessThreshhold                          = false;
	protected int     numberOfGenerationsAboveFitnessThresholdRequired = 0;
	protected int     currentNumberOfGenerationsAboveFitnessThreshold  = 0;
	
	protected Random    randomNumberGenerator;
		
	
	public Population(Simulator simulator) {
		super();
		this.randomNumberGenerator = simulator.getRandom();
		generationRandomSeed = randomNumberGenerator.nextInt();

	}


	/** 
     *   Create a new random population
     */
    public abstract void createRandomPopulation();


	/** 
	 * Check if an evolution is done
	 * 
     * @return true if the evolution is done, otherwise false
     */
    public abstract boolean evolutionDone();

    /** 
     *   Get the number of the current generation.
     *   
     *   @return the number of the current generation
     */
    public abstract int getNumberOfCurrentGeneration();

	/** 
     *   Get the number of chromosomes in the current generation.
     *   
     *   @return the size (number of chromosomes) of the current generation
     */
    public abstract int getPopulationSize();

	/** 
     *   Get the number of chromosomes that have already been evaluated in 
     *   the current generation.
     *   
     *   @return the number of chromosomes evaluated in the current generation.
     */
    public abstract int getNumberOfChromosomesEvaluated();

    /** 
     * Gets the next chromosome to evaluate. Returns the next chromosome if there 
     * are one or more chromosomes to evaluate at the time of the call. In case all 
     * the chromosomes have been/are being evaluated, null is returned.
     * <p>
     * Once the fitness has been computed for a chromosome use 
     * {@link #setEvaluationResult(Chromosome, double)} to set the result.
	 * <p>
  	 *  <b>Note</b>: Implementations of this method should set the generation 
  	 *  random seed before returning each individual. 
     */
    public abstract Chromosome getNextChromosomeToEvaluate();

    /**
     * Set the fitness for a chromosome obtained by a call to {@link #getNextChromosomeToEvaluate()}.
     * 
     * @param chromosome
     * @param fitness
     */
    public abstract void setEvaluationResult(Chromosome chromosome, double fitness);

     /**
     * Set the fitness for a chromosome in position pos obtained by a call to {@link #getNextChromosomeToEvaluate()}.
     * 
     * @param chromosome
     * @param fitness
     */
    public abstract void setEvaluationResultForId(int pos, double fitness);

    /** Creates the next generation. Once all of the chromosomes in a generation has been evaluated, call this
     *  method to create the next generation. This method should apply generation operators such as selection 
     *  mutation and cross-over etc.
     */	
    public abstract void createNextGeneration();

    /** Set the random seed before each chromosome is evaluated in the simulator.
     * 
     * @param seed the random seed for this generation.
     */
	public void setGenerationRandomSeed(int seed) {
		generationRandomSeed = seed;
	}
        
    /** Get the random seed to be used for the evaluation of each chromosome. 
     * 
     * @return the random seed for this generation.
     */

    public int getGenerationRandomSeed() {
		return generationRandomSeed;
    }
    
    /** Get the lowest fitness obtained in the current generation. 
     * 
     * @return the lowest fitness obtained in the generation.
     */
    public abstract double getLowestFitness();

    /** Get the average fitness obtained in the current generation. 
     * 
     * @return the average fitness obtained in the current generation.
     */
    public abstract double getAverageFitness();

    /** Get the highest fitness obtained in the current generation. 
     * 
     * @return the highest fitness obtained in the current generation.
     */
    public abstract double getHighestFitness();
      
    /** Get the chromosome that obtained the highest fitness in the current generation. 
     * 
     * @return chromosome that obtained the highest fitness in the current generation.
     */
    public abstract Chromosome getBestChromosome();

    /** Get the top chromosomes that obtained the highest fitness in the current generation. 
     * 
     * @param number of chromossomes to be returned
     * 
     * @return chromosome[] that obtained the highest fitness in the current generation.
     */
    public abstract Chromosome[] getTopChromosome(int number);

    /** Get the number of generations to evolve. 
     * 
     * @return the number generations to evolve.
     */
	public int getNumberOfGenerations() {
		return numberOfGenerations;
	}

    /** Set the number of generations to evolve. 
     * 
     * @param generations the number generations to evolve.
     */
    public void setNumberOfGenerations(int generations) {
    	this.numberOfGenerations = generations;
    }
	

	/** Get the number of fitness samples per chromosome. 
     * 
     * @return the number of fitness samples per chromosome.
     */
	public int getNumberOfSamplesPerChromosome() {
		if (enableIncreasingSamplesPerChromosome) {
			double samplesPerChromosome = increaseStartSamplesPerChromosome;
			samplesPerChromosome += (double) getNumberOfCurrentGeneration() * increaseAdditionalSamplesPerGeneration;
			if (samplesPerChromosome > increaseMaxSamplesPerChromosome) {
				samplesPerChromosome = increaseMaxSamplesPerChromosome;
			}
			return (int) samplesPerChromosome;
		} else {
			return numberOfSamplesPerChromosome;
		}
	}

	/** Set the mutation rate. 
     * 
     * @param mutationRate the mutation rate.
     */
	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	/** Get the number of simulation steps per fitness sample. 
     * 
     * @return number of simulation steps per fitness sample.
     */
	public int getNumberOfStepsPerSample() {
		if (enableIncreasingStepsPerSample) {
			double stepsPerSample = increaseStartStepsPerSample;
			stepsPerSample += (double) getNumberOfCurrentGeneration() * increaseAdditionalStepsPerSample;
			if (stepsPerSample > increaseMaxStepsPerSample) {
				stepsPerSample = increaseMaxStepsPerSample;
			}
			return (int) stepsPerSample;
		} else {
			return numberOfStepsPerSample;
		}
	}
	
	/** Set the number of fitness samples per chromosome. 
	 * 
	 * This will cancel any previously set increasing number of samples (see {@link #enableIncreasingSamplesPerChromosome}).     
	 * 
     * @param argumentAsInt the number of fitness samples per chromosome.
     */
    public void setNumberOfSamplesPerChromosome(int samples) {
    	this.numberOfSamplesPerChromosome = samples;
    }
   

	/** Set the number of fitness samples to increase throughout an evolution. This allows for 
	 * chromosomes to be sampled quickly (fewer samples) in the beginning of an evolutionary run and more accurately 
	 * later in an evolutionary run (more samples).
	 * 
	 * @param startSamples the number of samples for the first generation.
	 * @param maxSamples   the max number of samples -- the number of samples will not be increased beyond this number. 
     * @param additionalSamplesPerGeneration the number of additional samples per generation (can be a fraction).
     * 
     */
	public void setIncreasingSamplesPerFitnessSample(int startSamples,
			int maxSamples, double additionalSamplesPerGeneration) {
		this.enableIncreasingSamplesPerChromosome    = true;
		this.increaseStartSamplesPerChromosome       = startSamples;
		this.increaseMaxSamplesPerChromosome         = maxSamples;
		this.increaseAdditionalSamplesPerGeneration  = additionalSamplesPerGeneration;
	}


	/** Set the number of simulation steps per fitness sample to a fixed value. 
     *  This will cancel any previously set increasing number of step (see {@link #enableIncreasingStepsPerSample}).
     *
     * @param numberOfStepsPerSample number of simulation steps per fitness sample.
     */
	public void setNumberOfStepsPerSample(int numberOfStepsPerSample) {
		this.enableIncreasingSamplesPerChromosome = false;//TODO check this!
		this.enableIncreasingStepsPerSample   = false;
		this.numberOfStepsPerSample  			  = numberOfStepsPerSample;
	}

	/** Set the number of simulation steps per fitness sample to increase throughout the evolution. This allows for 
	 * chromosomes to be sampled quickly in the beginning of an evolutionary run and more accurately 
	 * later in an evolutionary run.
	 * 
	 * @param startStepsPerRun the number of steps per fitness sample for the first generation.
	 * @param maxSteps   the max number of steps -- the number of steps will not be increased beyond this number. 
     * @param additionalStepsPerRunPerGeneration the number of additional steps per generation (can be a fraction).
     * 
     */
	public void setIncreasingStepPerFitnessSample(int startStepsPerRun,
			int maxStepsPerRun, 
			double additionalStepsPerRunPerGeneration) {		
		this.enableIncreasingStepsPerSample   = true;
		this.increaseStartStepsPerSample      = startStepsPerRun;
		this.increaseMaxStepsPerSample        = maxStepsPerRun;
		this.increaseAdditionalStepsPerSample = additionalStepsPerRunPerGeneration;
	}

	/**
	* Enable fitness threshold-based stop of an evolution. If the best chromosome scores a fitness above the fitness 
	* threshold for a certain number of consecutive generations, the evolution is stopped. This function is useful for 
	* incremental evolutions where the task is made increasingly more difficult in order to bootstrap evolution.
	*
	* @param fitnessThreshold the fitness threshold value
	* @param numberOfGenerationsAboveFitnessThresholdRequired the number of generations the fitness has to be above the threshold before evolution is terminated.
	*/
	public void enableFitnessThreshold(double fitnessThreshold, 
									   int numberOfGenerationsAboveFitnessThresholdRequired) {
		enableFitnessThreshhold = true;
		this.fitnessThreshold   = fitnessThreshold;
		this.numberOfGenerationsAboveFitnessThresholdRequired = numberOfGenerationsAboveFitnessThresholdRequired;
		this.currentNumberOfGenerationsAboveFitnessThreshold  = 0;
	}

	/** 
	 * Disables fitness threshold-based termination of evolution.
	 * @see #enableFitnessThreshhold
	 */
	
	public void disableFitnessThreshold() {
		enableFitnessThreshhold = false;	
	}	

	/**
	 * Check if the fitness threshold has been reached. This method should be called by 
	 * a specialization of this class when all the chromosomes in a generation have been evaluated.
	 * If fitness-based threshold stops have not been enabled, it will return false. Otherwise, it will 
	 * perform the necessary checks and return true if the fitness threshold has been reached.
	 * 
	 * @param bestFitnessOfCurrentGeneration the best fitness of the current generation
	 * @return true if the fitness threshold has been reached
	 */
	protected boolean checkFitnessThreshold(double bestFitnessOfCurrentGeneration) {
		if (!enableFitnessThreshhold)
			return false;
		
		if (bestFitnessOfCurrentGeneration >= fitnessThreshold) {
			currentNumberOfGenerationsAboveFitnessThreshold++;
		} else {
			currentNumberOfGenerationsAboveFitnessThreshold = 0;			
		}
		
		return currentNumberOfGenerationsAboveFitnessThreshold >= numberOfGenerationsAboveFitnessThresholdRequired;
	}

	/**
	 * Parses a list of arguments and (re-)configures the population.
	 * 
	 * @param arguments
	 */
	
	public void parseArguments(Arguments arguments) {
		if (arguments.getArgumentIsDefined("stepsperrun")) {
			setNumberOfStepsPerSample(arguments.getArgumentAsInt("stepsperrun"));
		}
		
		if (arguments.getArgumentIsDefined("increasestepsperrun")) {
			setIncreasingStepPerFitnessSample(arguments.getArgumentAsInt("startsteps"), 
					arguments.getArgumentAsInt("endsteps"), 
					arguments.getArgumentAsDouble("stepspergeneration"));
		}

		if (arguments.getArgumentIsDefined("increasesamplesperindividual")) {
				setIncreasingSamplesPerFitnessSample(arguments.getArgumentAsInt("startsamples"), 
				arguments.getArgumentAsInt("endsamples"), 
				arguments.getArgumentAsDouble("samplespergeneration"));
		}

		if (arguments.getArgumentIsDefined("enablefitnessthreshold")) {
			enableFitnessThreshold(arguments.getArgumentAsDouble("fitnessthreshold"), 
					arguments.getArgumentAsInt("numberofgenerationsabovefitnessthresholdrequired"));
		}

		if (arguments.getArgumentIsDefined("mutationrate")) { 
			setMutationRate(arguments.getArgumentAsDouble("mutationrate"));
		}

		if (arguments.getArgumentIsDefined("samples")) {
			setNumberOfSamplesPerChromosome(arguments.getArgumentAsInt("samples"));
		}

		if (arguments.getArgumentIsDefined("generations")) {
			setNumberOfGenerations(arguments.getArgumentAsInt("generations"));
		}					
	}

}


