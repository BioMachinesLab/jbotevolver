package evolution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import evaluationfunctions.DistanceTravelledEvaluationFunction;
import evolutionaryrobotics.JBotEvolver;
import multiobjective.MOChromosome;
import multiobjective.MOTask;
import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.evaluators.FinalPositionWithOrientation3Behavior;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.util.Arguments;
import taskexecutor.results.SimpleFitnessResult;

public class NDBehaviorMap implements Serializable{
	
	private static final long serialVersionUID = 7350692636405490760L;
	protected int nDimensions;
	protected double[] limits;
	protected double[] resolutions;
	protected int[] sizes;
	protected HashMap<Integer,MOChromosome> map;
	protected double[][] buckets;
	protected boolean[] circularDimensions;
	
	protected int maxSize = 0;
	protected int behaviorIndex;
	
	protected double minAllele;
	protected double maxAllele;
	
	//after filling, this will indicate where the filling extends up to
	protected double circleRadius = 0;
	protected Arguments initArguments;
	
	public NDBehaviorMap(Arguments arguments) {
		initArguments = arguments;
		nDimensions = arguments.getArgumentAsInt("ndimensions");
		
		if(!arguments.getArgumentIsDefined("ndimensions")) throw new RuntimeException("Argument 'ndimensions' not specified in class NDBehaviorMap!");
		
		limits = new double[nDimensions];
		resolutions = new double[nDimensions];
		sizes = new int[nDimensions];
		
		String[] limitsStr = arguments.getArgumentAsString("limits").split(",");
		String[] resolutionsStr = arguments.getArgumentAsString("resolutions").split(",");
		
		for(int i = 0 ; i < nDimensions ; i++) {
			limits[i] = Double.parseDouble(limitsStr[i]);
			resolutions[i] = Double.parseDouble(resolutionsStr[i]);
			sizes[i] = (int)((limits[i]*2.0)/resolutions[i]);
			sizes[i]++;
			maxSize = Math.max(sizes[i], maxSize);
		}
		
		buckets = new double[nDimensions][];
		
		for(int n = 0 ; n < nDimensions ; n++) {
			buckets[n] = new double[sizes[n]];
			for(int i = 0 ; i < buckets[n].length ; i++) {
				double value = bucketToValue(n,i);
				buckets[n][i] = value;
			}
		}
		
		circularDimensions = new boolean[nDimensions];
		
		if(arguments.getArgumentIsDefined("minallele")) {
			this.minAllele = arguments.getArgumentAsDouble("minallele");
		}
		if(arguments.getArgumentIsDefined("maxallele")) {
			this.maxAllele = arguments.getArgumentAsDouble("maxallele");
		}
		
		if(arguments.getArgumentIsDefined("circular")) {
			String[] split = arguments.getArgumentAsString("circular").split(",");
			for(int i = 0 ; i < split.length ; i++) {
				circularDimensions[i] = split[i].equals("1");
			}
		}
		
		behaviorIndex = arguments.getArgumentAsInt("behavior-index");
		
		map = new HashMap<Integer,MOChromosome>();
	}
	
	//ADD
	public void addChromosome(MOChromosome moc, double[] vec) {
		map.put(getLocationHashFromBehaviorVector(vec),moc);
	}
	
	public void addChromosome(MOChromosome moc) {
		map.put(getLocationHash(moc),moc);
	}
	
	private void addChromosome(MOChromosome moc, int locationHash) {
		map.put(locationHash, moc);
	}
	
	//REMOVE
	public void removeChromosome(MOChromosome moc) {
		map.remove(getLocationHash(moc));
	}
	
	//GET
	public MOChromosome getChromosome(double[] vec) {
		return getChromosome(getLocationHashFromBehaviorVector(vec));
	}
	
	public MOChromosome getChromosome(int location) {
		return map.get(location);
	}
	
	public MOChromosome getChromosome(MOChromosome c) {
		return getChromosome(getLocationHash(c));
	}
	
	public int getNumberOfChromosomes() {
		return map.size();
	}
	
	public Collection<MOChromosome> getChromosomes() {
		return map.values();
	}

	//CONVERT
	public int getLocationHash(MOChromosome c) {
		double[] vec = getBehaviorVector(c);
		return getLocationHashFromBehaviorVector(vec);
	}
	
	public MOChromosome getChromosomeFromBehaviorVector(double[] vec) {
		int mapLocation = getLocationHashFromBehaviorVector(vec);
		return map.get(mapLocation);
	}
	
	public double[] getBehaviorVector(MOChromosome c) {
		ExpandedFitness fit = (ExpandedFitness)c.getEvaluationResult();
		return (double[])fit.getCorrespondingEvaluation(behaviorIndex).value();
	}
	
	public int[] getBucketsFromBehaviorVector(double[] vec) {
		int[] buckets = new int[vec.length];
		for(int d = 0 ; d < vec.length ; d++) {
			buckets[d] = valueToBucket(d, vec[d]);
		}
		return buckets;
	}
	
	public double[] getValuesFromBucketVector(int[] vec) {
		double[] values = new double[vec.length];
		for(int d = 0 ; d < vec.length ; d++) {
			values[d] = bucketToValue(d, vec[d]);
		}
		return values;
	}
	
	public String serialize() {
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(initArguments.getCompleteArgumentString());
		buffer.append("#");
		
		buffer.append(getCircleRadius()+" ");
		
		buffer.append(getNumberOfChromosomes()+" ");
		
		for(Integer hash : map.keySet()) {
			MOChromosome c = map.get(hash);
			buffer.append(c.getID()+" ");
			buffer.append(hash+" ");
			buffer.append(c.getAlleles().length+" ");
			
			for(int j = 0 ; j < c.getAlleles().length ; j++) {
				buffer.append(c.getAlleles()[j]+" ");
			}
			
			double[] vec = getBehaviorVector(c);
			
			ExpandedFitness fit = (ExpandedFitness)c.getEvaluationResult();
			buffer.append(fit.getFitness()+" ");
			buffer.append(vec.length+" ");
			for(int j = 0 ; j < vec.length ; j++) {
				buffer.append(vec[j]+" ");
			}
		}
		
		return buffer.toString();
	}
	
	public static NDBehaviorMap deserialize(String str) {
		
		Scanner s = new Scanner(str);
		Scanner localeScanner = s.useLocale(Locale.ENGLISH);
		
		String completeConfig = "";
		
		while(localeScanner.hasNextLine())
			completeConfig+="\n"+localeScanner.nextLine();
		
		String[] split = completeConfig.split("#");
		
		String arguments = split[0];
		
		NDBehaviorMap map = new NDBehaviorMap(new Arguments(arguments));
		
		localeScanner.close();
		s.close();
		
		s = new Scanner(split[1]);
		localeScanner = s.useLocale(Locale.ENGLISH);
		
		map.setCircleRadius(localeScanner.nextDouble());
		
		int numberOfChromosomes = localeScanner.nextInt();
		
		for(int i = 0 ; i < numberOfChromosomes ; i++) {
			int id = localeScanner.nextInt();
			int hash = localeScanner.nextInt();
			double[] alleles = new double[localeScanner.nextInt()];
			
			for(int j = 0 ; j < alleles.length ; j++) {
				alleles[j] = localeScanner.nextDouble();
			}
			
			double fitness = localeScanner.nextDouble();
			double[] vec = new double[localeScanner.nextInt()];
			for(int j = 0 ; j < vec.length ; j++) {
				vec[j] = localeScanner.nextDouble();
			}
			
			ExpandedFitness fit = new ExpandedFitness();
			fit.setFitness(fitness);
			
			ArrayList<EvaluationResult> evals = new ArrayList<EvaluationResult>();
			
			for(int j = 0 ; j < map.behaviorIndex ; j++)
				evals.add(new FitnessResult(fitness));
			evals.add(new VectorBehaviourResult(vec));
			
			fit.setEvaluationResults(evals);
			
			MOChromosome moc = new MOChromosome(alleles, id);
			moc.setEvaluationResult(fit);
			
			map.addChromosome(moc, hash);
		}
		localeScanner.close();
		s.close();
		return map;
	}
	
	//gets the N-dimensional hash
	public int getLocationHashFromBehaviorVector(double[] vec) {

		if(vec.length != nDimensions)
			throw new RuntimeException("Behavior characterisation different from map dimensions! [NDMAPElitesPopulation]");
		
		int result = 0;
		
		for(int i = 0 ; i < vec.length ; i++) {
			
			if(vec[i] < getBucketListing(i)[0]) {
				vec[i]+=2.0*limits[i];
			}
			
			int v = valueToBucket(i,vec[i]);
			result+= v * Math.pow(maxSize,i);
		}
		
		return result;
	}
	
	public double bucketToValue(int dim, int b) {
		return ((double)b - (double)sizes[dim]/2.0)*resolutions[dim];
	}
	
	public int valueToBucket(int dim, double v) {
		return (int)((double)sizes[dim]/2.0 + v/resolutions[dim]);
	}
	
	//GETTERS
	
	//returns the locations of the buckets for dimension "n"
	public double[] getBucketListing(int n) {
		return buckets[n];
	}

	public double[] getResolutions() {
		return resolutions;
	}
	
	public double[] getLimits() {
		return limits;
	}
	
	public int[] getSizes() {
		return sizes;
	}
	
	public boolean getCircularDimension(int dim) {
		return circularDimensions[dim];
	}
	
	public boolean[] getCircularDimensions() {
		return circularDimensions;
	}
	
	public int getNDimensions() {
		return nDimensions;
	}
	
	public void setCircleRadius(double circleRadius) {
		this.circleRadius = circleRadius;
	}
	
	public double getCircleRadius() {
		return circleRadius;
	}

	public void setMinMaxAllele(double minAllele, double maxAllele) {
		this.minAllele = minAllele;
		this.maxAllele = maxAllele;
		this.initArguments.setArgument("minallele", minAllele);
		this.initArguments.setArgument("maxallele", maxAllele);
	}
	
	public double getMinAllele() {
		return minAllele;
	}
	
	public double getMaxAllele() {
		return maxAllele;
	}
}