/*
 * Created on 22-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package evolutionaryrobotics.evolution.neat.core;

import evolutionaryrobotics.evolution.neat.ga.core.Gene;

/**
 * Extension of the Gene interface to provide more specific NEAT behaviour
 * @author MSimmerson
 *
 */
public interface NEATGene extends Gene {
	public int getInnovationNumber();
}
