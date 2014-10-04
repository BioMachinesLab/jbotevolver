package neat.evaluation;

import java.io.Serializable;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.score.AdjustScore;

public class MinimumScoreAdjuster implements AdjustScore, Serializable{
	
	private double minFitness = 0;
	
	public MinimumScoreAdjuster(double minFitness) {
		this.minFitness = minFitness < 0 ? -minFitness : 0;
	}
	
	@Override
	public double calculateAdjustment(Genome genome) {
		return minFitness;
	}

}
