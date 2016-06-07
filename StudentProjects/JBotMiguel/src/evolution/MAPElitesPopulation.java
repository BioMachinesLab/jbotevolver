package evolution;

import java.util.ArrayList;

import multiobjective.MOChromosome;
import novelty.ExpandedFitness;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class MAPElitesPopulation extends Population{
	
	private static final long serialVersionUID = -4634842347856072534L;
	
	protected MOChromosome[][] map;
	protected double resolution;
	protected double limit;
	protected int currentGeneration;
	protected int generations;
	protected int initialBatch = 20000;
	protected int batchSize = 2000;
	protected int genomeLength;
	protected int currentChromosomeId;
	protected int behaviorIndex;
	protected int fitnessIndex;
	protected int numberOfChromosomesRequested;
	protected int earlyTerminationGenerations = 0;
	protected int stagnatedGenerations = 0;
	protected ArrayList<MOChromosome> chromosomes;
	protected boolean randomMutation = false;
	protected double gaussianStdDev = 1.0;
	
	protected double maxAllele = 10;
	protected double minAllele = -10;
	
	protected double[] possibleValues;

	public MAPElitesPopulation(Arguments arguments) {
		super(arguments);
		
		initialBatch = arguments.getArgumentAsInt("initialbatch");
		batchSize = arguments.getArgumentAsInt("batchsize");
		numberOfGenerations = arguments.getArgumentAsInt("generations");
		genomeLength = arguments.getArgumentAsInt("genomelength");
		behaviorIndex = arguments.getArgumentAsInt("behavior-index");
		fitnessIndex = arguments.getArgumentAsInt("fitness-index");
		randomMutation = arguments.getFlagIsTrue("randommutation");
		
		gaussianStdDev = arguments.getArgumentAsDoubleOrSetDefault("gaussian", gaussianStdDev);

		limit = arguments.getArgumentAsDouble("limit");
		resolution = arguments.getArgumentAsDouble("resolution");

		int size = (int)(limit/resolution);
		map = new MOChromosome[size][size];
		
		chromosomes = new ArrayList<MOChromosome>();
		
		if(arguments.getArgumentIsDefined("maxallele")) {
			maxAllele = arguments.getArgumentAsInt("maxallele");
			minAllele = -maxAllele;
		}
		
		if(arguments.getArgumentIsDefined("minallele")) {
			minAllele = arguments.getArgumentAsInt("minallele");
		}
		
		if(arguments.getArgumentIsDefined("possiblevalues")) {
			String values = arguments.getArgumentAsString("possiblevalues");
			String[] split = values.split(",");
			possibleValues = new double[split.length];
			int index = 0;
			for(String s : split) {
				possibleValues[index++] = Double.parseDouble(s);
			}
		}
		
		if(arguments.getArgumentIsDefined("earlyterminationgenerations")) {
			earlyTerminationGenerations = arguments.getArgumentAsInt("earlyterminationgenerations");
		}
		
	}
	
	public ArrayList<MOChromosome> getRandomPopulation() {
		//MAP-Elites requires evaluation before the individuals can be placed in the map
		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		ArrayList<MOChromosome> chromosomes = new ArrayList<MOChromosome>(initialBatch);
		
		for (int i = 0; i < initialBatch; i++) {
			double[] alleles = new double[genomeLength];
			for (int j = 0; j < genomeLength; j++) {
				
				if(possibleValues != null) {
					alleles[j] = possibleValues[randomNumberGenerator.nextInt(possibleValues.length)];
				} else {
					double interval = maxAllele-minAllele; 
					double val = randomNumberGenerator.nextDouble()*interval;
					alleles[j] = val+minAllele;
				}
			}
			chromosomes.add(new MOChromosome(alleles, currentChromosomeId++));
		}

		setGenerationRandomSeed(randomNumberGenerator.nextInt());
		return chromosomes;
	}
	
	public MOChromosome getMutatedChromosome() {
		numberOfChromosomesRequested++;
		MOChromosome randomChromosome = chromosomes.get(randomNumberGenerator.nextInt(chromosomes.size()));
		return mutate(randomChromosome);
	}
	
	public int getNumberOfChromosomes() {
		return chromosomes.size();
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	
	public double getMapResolution() {
		return resolution;
	}
	
	public void addToMapForced(MOChromosome moc, int[] pos) {
		map[pos[0]][pos[1]] = moc;
		chromosomes.add(moc);
	}
	
	public void addToMap(MOChromosome moc) {
		int[] mocMapLocation = positionInMap(moc);
		
		if(mocMapLocation[0] >= map.length || mocMapLocation[0] < 0 ||
				mocMapLocation[1] >= map[mocMapLocation[0]].length || mocMapLocation[1] < 0)
			return;
		
 		MOChromosome currentMapIndividual = map[mocMapLocation[0]][mocMapLocation[1]];
		
		if(currentMapIndividual == null) {
			//the cell is empty, add immediately
			map[mocMapLocation[0]][mocMapLocation[1]] = moc;
			chromosomes.add(moc);
			stagnatedGenerations = 0;
		} else {
			//check fitness performance to figure out if current chromosome has to be replace
			if(getFitness(moc) > getFitness(currentMapIndividual)) {
				map[mocMapLocation[0]][mocMapLocation[1]] = moc;
				int index = chromosomes.indexOf(currentMapIndividual);
				chromosomes.set(index, moc);
				stagnatedGenerations = 0;
			} else {
			}
		}
	}
	
	public void removeFromMap(MOChromosome moc) {
		int index = chromosomes.indexOf(moc);
		chromosomes.remove(index);
		int[] loc = positionInMap(moc);
		map[loc[0]][loc[1]] = null;
	}
	
	protected double getFitness(MOChromosome moc) {
		
		double fitness = 0;
		
		ExpandedFitness fit = (ExpandedFitness)moc.getEvaluationResult();
		fitness = (double)fit.getCorrespondingEvaluation(fitnessIndex).value();
		
		return fitness;
	}
	
	public MOChromosome getChromosomeFromBehaviorVector(double[] vec) {
		int[] mapLocation = getLocationFromBehaviorVector(vec);
		return map[mapLocation[0]][mapLocation[1]];
	}
	
	public int[] getLocationFromBehaviorVector(double[] vec) {
		int[] mapLocation = new int[vec.length];
		
		for(int i = 0 ; i < vec.length ; i++) {
			int pos = (int)(vec[i]/resolution + map.length/2.0);
			mapLocation[i] = pos;
		}
		return mapLocation;
	}
	
	public int[] positionInMap(MOChromosome c) {
		double[] vec = getBehaviorVector(c);
		return getLocationFromBehaviorVector(vec);
	}
	
	public double[] getBehaviorVector(MOChromosome c) {
		ExpandedFitness fit = (ExpandedFitness)c.getEvaluationResult();
		return (double[])fit.getCorrespondingEvaluation(behaviorIndex).value();
	}
	
	protected MOChromosome mutate(MOChromosome c) {
		
		double[] alleles = new double[c.getAlleles().length];
		
		boolean mutated = false;
		
		do {
		
			for (int j = 0; j < c.getAlleles().length; j++) {
				double allele = c.getAlleles()[j];
				if (randomNumberGenerator.nextDouble() < mutationRate) {
					mutated = true;
					if(possibleValues != null) {
						double currentValue = allele;
						do {
							allele = possibleValues[randomNumberGenerator.nextInt(possibleValues.length)];
						} while(allele == currentValue);
					} else if(!randomMutation)
						allele = allele + randomNumberGenerator.nextGaussian()*gaussianStdDev;
					else {
						double interval = maxAllele-minAllele; 
						double val = randomNumberGenerator.nextDouble()*interval;
						allele = val+minAllele;
					}
					
					if (allele < -maxAllele)
						allele = -maxAllele;
					if (allele > maxAllele)
						allele = maxAllele;
				}
				alleles[j] = allele;
			}
		}while(!mutated);
		
		return new MOChromosome(alleles, currentChromosomeId++);
	}
	
	public MOChromosome[][] getMap() {
		return map;
	}
	
	@Override
	public boolean evolutionDone() {
		return (currentGeneration >= numberOfGenerations) ||
				(currentGeneration == numberOfGenerations-1 && getNumberOfChromosomesEvaluated() == batchSize) ||
				(earlyTerminationGenerations > 0 && stagnatedGenerations >= earlyTerminationGenerations); 
	}
	
	@Override
	public void createNextGeneration() {
		currentGeneration++;
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
		numberOfChromosomesRequested = 0;
		stagnatedGenerations++;
	}
	
	@Override
	public int getPopulationSize() {
		return batchSize;
	}
	
	@Override
	public double getHighestFitness() {
		return chromosomes.size();
	}
	
	@Override
	public int getNumberOfCurrentGeneration() {
		return currentGeneration;
	}
	
	@Override
	public double getAverageFitness() {
		double fitness = 0;
		
		for(MOChromosome c : chromosomes) {
			fitness+=getFitness(c)/chromosomes.size();
		}
		
		return fitness;
	}
	
	@Override
	public Chromosome[] getChromosomes() {
		return chromosomes.toArray(new Chromosome[chromosomes.size()]);
	}
	
	@Override
	public Chromosome getBestChromosome() {
		
		MOChromosome best = chromosomes.get(0);
		
		for(MOChromosome moc : chromosomes) {
			if(getFitness(moc) > getFitness(best))
				best = moc;
		}
		return best;
	}
	
	public int getGenomeLength() {
		return genomeLength;
	}
	
	@Override
	public int getNumberOfChromosomesEvaluated() {
		return this.numberOfChromosomesRequested;
	}
	
	public double getDistanceLimit() {
		return limit;
	}
	
	@Override
	public void createRandomPopulation() {}
	@Override
	public Chromosome getChromosome(int chromosomeId) {return null;}
	@Override
	public double getLowestFitness() {return 0;}
	@Override
	public Chromosome getNextChromosomeToEvaluate() {return null;}
	@Override
	public Chromosome[] getTopChromosome(int number) {return null;}
	@Override
	public void setEvaluationResult(Chromosome chromosome, double fitness) {}
	@Override
	public void setEvaluationResultForId(int pos, double fitness) {}
	
	@Override
	public void setupIndividual(Robot r) {Chromosome c = getBestChromosome();
		if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController fc = (FixedLenghtGenomeEvolvableController)r.getController();
			if(fc.getNNWeights() == null) {
				fc.setNNWeights(c.getAlleles());
			}
		}
	}
}