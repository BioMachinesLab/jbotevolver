package evolutionaryrobotics.evolution.neat;

import java.util.Map;
import evolutionaryrobotics.evolution.neat.core.NEATFitnessFunction;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class PreEvaluatedFitnessFunction extends NEATFitnessFunction {

    private final Map<Chromosome, Float> scores;

    public PreEvaluatedFitnessFunction(Map<Chromosome, Float> scores) {
        super(null, null);
        this.scores = scores;
    }

    @Override
    public double evaluate(Chromosome genoType) {
        Float sc = scores.get(genoType);
        if (sc == null) {
            throw new RuntimeException("Genotype not previously evaluated!");
        } else {
            return sc;
        }
    }
}