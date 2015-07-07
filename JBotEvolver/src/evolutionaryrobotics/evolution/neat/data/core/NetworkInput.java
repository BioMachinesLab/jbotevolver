/*
 * Created on Oct 1, 2004
 *
 */
package evolutionaryrobotics.evolution.neat.data.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface NetworkInput extends Serializable {
	public double[] pattern();
}
