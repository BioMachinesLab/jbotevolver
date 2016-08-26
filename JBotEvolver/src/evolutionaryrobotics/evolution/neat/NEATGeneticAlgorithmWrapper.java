package evolutionaryrobotics.evolution.neat;

import simulation.util.Arguments;
import simulation.util.Factory;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.GenerationalTask;
import tasks.Task;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATGeneticAlgorithm;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATPopulation;

public class NEATGeneticAlgorithmWrapper extends NEATGeneticAlgorithm {

	protected NEATEvolution evo;

	public NEATGeneticAlgorithmWrapper(NEATGADescriptor descriptor, NEATEvolution evo) {
		super(descriptor);
		this.evo = evo;
	}

	@Override
	protected void evaluatePopulation(Chromosome[] genotypes) {
		int i;

		evolutionaryrobotics.neuralnetworks.Chromosome[] convertedGenotypes = new evolutionaryrobotics.neuralnetworks.Chromosome[genotypes.length];

		for (i = 0; i < genotypes.length && evo.continueExecuting(); i++) {

			int samples = evo.getPopulation().getNumberOfSamplesPerChromosome();
			Chromosome neatChromosome = genotypes[i];

			evolutionaryrobotics.neuralnetworks.Chromosome jBotChromosome = ((NEATPopulation) evo.getPopulation())
					.convertChromosome(neatChromosome, i);

			convertedGenotypes[i] = jBotChromosome;

			String taskClass = GenerationalTask.class.getName();
			
			if (evo.getJBotEvolver().getArgumentsCopy().get("--evolution").getArgumentIsDefined("task")) {
				Arguments evolutionArguments = new Arguments(
						evo.getJBotEvolver().getArgumentsCopy().get("--evolution").getArgumentAsString("task"));
				if (evolutionArguments.getArgumentIsDefined("classname")) {
					taskClass = evolutionArguments.getArgumentAsString("classname");
				}
			}
			
			Task task = (Task) Factory.getInstance(taskClass,evo.getJBotEvolver().getCopy(),
					samples, jBotChromosome, evo.getPopulation().getGenerationRandomSeed());

			// GenerationalTask neatTask = new GenerationalTask(
			// new JBotEvolver(evo.getJBotEvolver().getArgumentsCopy(),
			// evo.getJBotEvolver().getRandomSeed()),
			// samples, jBotChromosome,
			// evo.getPopulation().getGenerationRandomSeed());
			// System.out.println(evo.getJBotEvolver().getArgumentsCopy());

			evo.getTaskExecutor().addTask(task);
			System.out.print(".");
		}

		System.out.println();

		for (i = 0; i < genotypes.length && evo.continueExecuting(); i++) {
			SimpleFitnessResult r = (SimpleFitnessResult) evo.getTaskExecutor().getResult();
			evo.getPopulation().setEvaluationResult(convertedGenotypes[r.getChromosomeId()], r.getFitness());
			genotypes[r.getChromosomeId()].updateFitness(r.getFitness());
			System.out.print("!");
		}
	}

}