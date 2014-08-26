package neatCompatibilityImplementation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ControllersStatistics<E> implements MOStatistics<E>, Serializable{

	protected HashMap<E, TaskStatistics> map;
	
	public ControllersStatistics(){
		this.map = new HashMap<E, TaskStatistics>();
	}
	
	@Override
	public Map<E, TaskStatistics> getObjectivesStatistics() {
		return map;
	}

	@Override
	public void resetStatistics() {
		this.map.clear();
	}

	public void addControllerStatistics(E e, TaskStatistics stats){
		this.map.put(e, stats);
	}
	
	public TaskStatistics getStatisticsOfElement(E element){
		return map.get(element);
	}
}
