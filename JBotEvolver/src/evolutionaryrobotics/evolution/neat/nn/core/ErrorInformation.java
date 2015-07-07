/*
 * Created on Sep 30, 2004
 *
 */
package evolutionaryrobotics.evolution.neat.nn.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface ErrorInformation  extends Serializable {
	public double[] error();
}
