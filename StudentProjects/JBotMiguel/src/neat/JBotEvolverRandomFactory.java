package neat;

import java.io.Serializable;
import java.util.Random;

import org.encog.mathutil.randomize.factory.RandomFactory;

public class JBotEvolverRandomFactory implements RandomFactory, Serializable {
	
	private long currentSeed = 0;
	
	public JBotEvolverRandomFactory(long seed) {
		this.currentSeed = seed;
	}
	
	public JBotEvolverRandomFactory() {}

	@Override
	public Random factor() {
		return new Random(currentSeed);
	}

	@Override
	public RandomFactory factorFactory() {
		return this;
	}
	
	public void setCurrentSeed(long currentSeed) {
		this.currentSeed = currentSeed;
	}
}