package neat.evaluation;

import java.util.Map;

public interface MOStatistics<E> {

	public Map<E, TaskStatistics> getObjectivesStatistics();

	public void resetStatistics();
}
