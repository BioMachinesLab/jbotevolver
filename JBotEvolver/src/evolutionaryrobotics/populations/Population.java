package evolutionaryrobotics.populations;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import simulation.util.Factory;
import comm.FileProvider;
import evolutionaryrobotics.neuralnetworks.Chromosome;

/**
 * Super-class for populations/evolutionary algorithms.
 * 
 * @author alc
 */
public abstract class Population implements Serializable {
	//the UID is important for backwards compatibility
	private static final long serialVersionUID = 5558128741655037815L;

	protected long generationRandomSeed;
	@ArgumentsAnnotation(name="samples", defaultValue="5")
	protected int numberOfSamplesPerChromosome;
	@ArgumentsAnnotation(name="generations", defaultValue="100")
	protected int numberOfGenerations    = 100;

	protected boolean enableIncreasingSamplesPerChromosome = false;
	protected int     increaseStartSamplesPerChromosome;
	protected int     increaseMaxSamplesPerChromosome;      
	protected double  increaseAdditionalStepsPerSample;
	
	protected boolean enableIncreasingStepsPerSample          = false;
	protected int     increaseStartStepsPerSample;
	protected int     increaseMaxStepsPerSample;
	protected double  increaseAdditionalSamplesPerGeneration;
	@ArgumentsAnnotation(name="mutationrate", defaultValue="0.1")
	protected double  mutationRate;
	
	protected double  fitnessThreshold                                 = 0;
	protected boolean enableFitnessThreshhold                          = false;
	protected int     numberOfGenerationsAboveFitnessThresholdRequired = 0;
	protected int     currentNumberOfGenerationsAboveFitnessThreshold  = 0;
	
	protected Random    randomNumberGenerator;
	
	private LinkedList<Serializable> serializableObjects;
	
	public Population(Arguments arguments) {
		this.randomNumberGenerator = new Random(0);
		
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
		
		serializableObjects = new LinkedList<Serializable>();
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
	public void setGenerationRandomSeed(long seed) {
		generationRandomSeed = seed;
	}
        
    /** Get the random seed to be used for the evaluation of each chromosome. 
     * 
     * @return the random seed for this generation.
     */

    public long getGenerationRandomSeed() {
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
	
	public synchronized static Population getPopulation(Arguments args) throws Exception {
		
		if(args.getArgumentIsDefined("load"))
			return loadPopulationFromFile(args);
		
		if (!args.getArgumentIsDefined("classname"))
			throw new RuntimeException("Population 'classname' not defined: "+args.toString());
		
		return (Population)Factory.getInstance(args.getArgumentAsString("classname"),args);
	}
	
	public synchronized static Population getCoEvolutionPopulations(Arguments args, String name) throws Exception {
		if(args.getArgumentIsDefined("load" + name))
			return loadSpecificPopulationFromFile(args, name);
		
		if (!args.getArgumentIsDefined("classname"))
			throw new RuntimeException("Population 'classname' not defined: "+args.toString());
		
		return (Population)Factory.getInstance(args.getArgumentAsString("classname"),args);
	}

	private static Population loadPopulationFromFile(Arguments args) throws Exception{
		File f = new File(args.getArgumentAsString("load"));
		
		File populationFile = new File(args.getArgumentAsString("parentfolder")+"/populations/"+f.getName());
		
		if(!populationFile.exists())
			populationFile = new File(args.getArgumentAsString("parentfolder")+"/../populations/"+f.getName());
		
		FileInputStream fis = new FileInputStream(populationFile);
		GZIPInputStream gzipIn = new GZIPInputStream(fis);
		ObjectInputStream in = new ObjectInputStream(gzipIn);
		
		try {
			Object obj = in.readObject();
			Population population = (Population) obj;
			in.close();
			return population;
		} catch(OptionalDataException e) {
			e.printStackTrace();
			System.err.println("There was a problem opening "+f.getName()+"! Opening previous population file...");
			String load = args.getArgumentAsString("load");
			int i;
			for(i = load.length() - 1 ; i >= 0 ; i--) {
				char c = load.charAt(i);
				if(c < '0' || c > '9')
					break;
			}
			
			int gen = (Integer.parseInt(load.substring(i+1))-1);
			if(gen == 0)
				throw new RuntimeException("First generation is broken!");
			String prev = load.substring(0,i+1) + gen;
			args.setArgument("load", prev);
			return loadPopulationFromFile(args);
		}
	}
	
	private static Population loadSpecificPopulationFromFile(Arguments args, String name) throws Exception{
		
		FileProvider fp = FileProvider.getDefaultFileProvider();
		File f = new File(args.getArgumentAsString("load" + name.toLowerCase()));
		
		File populationFile = new File(args.getArgumentAsString("parentfolder")+"/populations/"+f.getName());
		
		if(!populationFile.exists())
			populationFile = new File(args.getArgumentAsString("parentfolder")+"/../populations/"+f.getName());
		
		FileInputStream fis = new FileInputStream(populationFile);
		GZIPInputStream gzipIn = new GZIPInputStream(fis);
		ObjectInputStream in = new ObjectInputStream(gzipIn);
		Population population = (Population) in.readObject();
		in.close();
		return population;
	}

	public abstract Chromosome getChromosome(int chromosomeId);
	
	public LinkedList<Serializable> getSerializableObjects() {
		return serializableObjects;
	}
	
	public abstract void setupIndividual(Robot r);
	
	public abstract Chromosome[] getChromosomes();
	
}