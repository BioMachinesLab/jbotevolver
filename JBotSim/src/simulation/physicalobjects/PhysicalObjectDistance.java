package simulation.physicalobjects;

import java.io.Serializable;

public class PhysicalObjectDistance implements
		Comparable<PhysicalObjectDistance>, Serializable {
	private PhysicalObject object;
	private Double time;
	private Double lastDistance = 0.0;

	public PhysicalObjectDistance(PhysicalObject object, Double time) {
		super();
		this.object = object;
		this.time = time;
	}

	public Double getTime() {
		return time;
	}

	public void setTime(Double time) {
		this.time = time;
	}

	public PhysicalObject getObject() {
		return object;
	}
	
	public Double getLastDistance() {
		return lastDistance;
	}
	
	public void setLastDistance(Double lastDistance) {
		this.lastDistance = lastDistance;
	}

	@Override
	public boolean equals(Object o) {
		return object.getId() == ((PhysicalObjectDistance) o).object.getId();
	}

	// @Override
	public int compareTo(PhysicalObjectDistance o) {
		return object.compareTo(o.object);
	}

	@Override
	public String toString() {
		return "[object=" + object + ", time=" + time + "]";
	}

}