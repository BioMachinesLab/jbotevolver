package evolution;

import java.io.Serializable;
import java.util.HashMap;

import novelty.ExpandedFitness;
import multiobjective.MOChromosome;
import simulation.util.Arguments;

public class NDBehaviorMap implements Serializable{
	
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
	
	public NDBehaviorMap(Arguments arguments) {
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
	}
	
	public double getMinAllele() {
		return minAllele;
	}
	
	public double getMaxAllele() {
		return maxAllele;
	}
}