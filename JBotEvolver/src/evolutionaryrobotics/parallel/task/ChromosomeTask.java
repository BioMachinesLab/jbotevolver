package evolutionaryrobotics.parallel.task;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.parallel.ClientDescription;

public class ChromosomeTask implements Task {
	private Chromosome chromosome;
	private ClientDescription clientDescription;

	public ChromosomeTask(Chromosome chromosome,
			ClientDescription clientDescription) {
		super();
		this.chromosome = chromosome;
		this.clientDescription = clientDescription;
	}

	public Chromosome getChromosome() {
		return chromosome;
	}

	public ClientDescription getClientDescription() {
		return clientDescription;
	}

	public Arguments[] getArgumets() {
		return clientDescription.getArguments();
	}

	public int getNumberOfSamplesPerChromosome() {
		return clientDescription.getPopulation()
				.getNumberOfSamplesPerChromosome();
	}

	public int getNumberOfStepsPerSample() {
		return clientDescription.getPopulation().getNumberOfStepsPerSample();
	}

	public void setEvaluationResult(double fitness, int timeStamp) {
		clientDescription.setEvaluationResult(chromosome, fitness, timeStamp);
	}

	public Long getRandom() {
		// TODO Auto-generated method stub
		return new Long(clientDescription.getPopulation().getGenerationRandomSeed());
	}

}
