package controllers;

public interface FixedLenghtGenomeEvolvableController {
	public void setNNWeights(double[] weights);
	public int getGenomeLength();
	public double[] getNNWeights();
	public int getNumberOfInputs();
	public int getNumberOfOutputs();
}
