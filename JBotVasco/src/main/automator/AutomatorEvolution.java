package main.automator;

import evolutionaryrobotics.NEATTargetPostEvaluation;
import simulation.util.Arguments;
import src.Controller;
import src.Evolution;
import src.Main;

public class AutomatorEvolution extends Evolution {
	private NEATTargetPostEvaluation p;

	public AutomatorEvolution(Main main, Controller controller, String arguments) {
		super(main, controller, arguments);
	}

	@Override
	protected int runPostEvaluation() throws Exception {
		int toReturn = super.runPostEvaluation();

		Thread.sleep(2000); // Make sure that all files have been written to the
							// Disk

		if (p == null) {
			Arguments postArguments = controller.getArguments("--postevaluation");
			String stringArguments = "dir=" + outputFolder;
			if (postArguments != null) {
				for (String arg : postArguments.getArguments())
					stringArguments += " " + arg + "=" + postArguments.getArgumentAsString(arg);
			}
			p = new NEATTargetPostEvaluation(stringArguments.split(" "));
		}
		
		System.out.printf("[%s] Finished post evolution, starting metrics collection%n",getClass().getSimpleName());
		p.runMetricsEval();

		return toReturn;
	}

	@Override
	protected int runNeatPostEvaluation(int nRuns, int fitnessSamples, String stringArguments) throws Exception {
		p = new NEATTargetPostEvaluation(stringArguments.split(" "));
		double[][][] values = p.runPostEval();

		int[] bestGenerationIndex = new int[nRuns];
		double[] bestFitness = new double[nRuns];
		double[][] generationAverage = new double[nRuns][values[0].length];
		double[][] generationStdDeviations = new double[nRuns][values[0].length];

		int bestRunIndex = 0;

		for (int run = 0; run < values.length; run++) {
			double val = 0;

			for (int generation = 0; generation < values[run].length; generation++) {

				val = 0;

				for (int fitnesssample = 0; fitnesssample < values[run][generation].length; fitnesssample++) {
					val += values[run][generation][fitnesssample];
				}

				generationAverage[run][generation] = getAverage(values[run][generation]);
				generationStdDeviations[run][generation] = getStdDeviation(values[run][generation],
						generationAverage[run][generation]);

				val /= values[run][generation].length;

				if (val > bestFitness[run]) {
					bestGenerationIndex[run] = generation;
					bestFitness[run] = val;
				}
			}

			if (bestFitness[run] > bestFitness[bestRunIndex]) {
				bestRunIndex = run;
			}

		}

		int best = bestRunIndex + 1;
		double overallAverage = 0;

		String result = "#best: " + best + "\n";
		for (int run = 0; run < nRuns; run++) {
			result += (run + 1) + " ";

			for (double fitnessSampleVal : values[run][bestGenerationIndex[run]])
				result += fitnessSampleVal + " ";

			overallAverage += generationAverage[run][bestGenerationIndex[run]] / nRuns;

			result += " (" + generationAverage[run][bestGenerationIndex[run]] + " +- "
					+ generationStdDeviations[run][bestGenerationIndex[run]] + ") " + bestGenerationIndex[run] + "\n";
		}

		result += "Overall: " + overallAverage + " +- " + getOverallStdDeviation(generationAverage, nRuns);
		createFile(outputFolder, "post.txt", result);

		System.out.printf("[%s] %s%n", getClass().getSimpleName(), result);
		return best;
	}
}