package evolutionaryrobotics.evolution.neat.data.core;

/**
 * @author MSimmerson
 *
 */
public interface ExpectedOutputSet extends NetworkOutputSet{
	public NetworkOutput outputAt(int idx);		
}
