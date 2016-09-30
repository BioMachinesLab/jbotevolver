package main;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;

public class FitnessAnalyser {
	private static final String path = "./experiments/debug_run/";
	private static final String fileName = "_showbest_current.conf";
	private static final int SEED = 1;

	public static void main(String[] args) {
		// new FitnessAnalyser(SEED);

		for (int i = 0; i < 100; i++) {
			// System.out.println("##################");
			FitnessAnalyser analyser = new FitnessAnalyser(i);

			if (analyser.getFinalFitness() < 10000) {
				System.out.printf("Seed: %d\tInit fitness: %f\tFinal fitness: %f%n", i, analyser.getInitialFitness(),
						analyser.getFinalFitness());
			} else {
				System.err.printf("Seed: %d\tInit fitness: %f\tFinal fitness: %f%n", i, analyser.getInitialFitness(),
						analyser.getFinalFitness());
			}
		}
	}

	private double initialFitness = -1;
	private double finalFitness = -1;
	private boolean simulationConcluded = false;

	public FitnessAnalyser(int seed) {
		try {
			JBotEvolver jBot = new JBotEvolver(null);
			jBot.loadFile(path + fileName, "--random-seed " + seed);

			if (jBot.getRandomSeed() != seed) {
				throw new IllegalStateException("The random seed does not match the introduced one!");
			}

			EvaluationFunction evaluationFuntion = jBot.getEvaluationFunction()[0];
			initialFitness = evaluationFuntion.getFitness();

			Simulator simulator = jBot.createSimulator();
			jBot.setupBestIndividual(simulator);
			simulator.setupEnvironment();
			simulator.addCallback(evaluationFuntion);
			simulator.simulate();
			finalFitness = evaluationFuntion.getFitness();
			simulationConcluded = true;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getInitialFitness() {
		return initialFitness;
	}

	public double getFinalFitness() {
		return finalFitness;
	}
	
	public boolean isSimulationConcluded() {
		return simulationConcluded;
	}
}
