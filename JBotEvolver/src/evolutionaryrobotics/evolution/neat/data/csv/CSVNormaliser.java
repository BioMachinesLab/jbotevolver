/*
 * Created on 15-Oct-2004
 *
 */
package evolutionaryrobotics.evolution.neat.data.csv;

import evolutionaryrobotics.evolution.neat.data.core.NetworkDataSet;
import evolutionaryrobotics.evolution.neat.data.core.Normaliser;

/**
 * @author MSimmerson
 *
 */
public class CSVNormaliser implements Normaliser {

	/**
	 * @see org.neat4j.ailibrary.nn.data.Normaliser#normalise(org.neat4j.ailibrary.nn.data.NetworkDataSet)
	 */
	@Override
	public NetworkDataSet normalise(NetworkDataSet dataSet) {
		
		return (dataSet);
	}

}
