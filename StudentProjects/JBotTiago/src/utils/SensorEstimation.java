package utils;

import mathutils.Vector2d;

public class SensorEstimation {

	private int targetId;
	private Vector2d estimation;
	
	public SensorEstimation(int targetId, Vector2d estimation) {
		this.targetId = targetId;
		this.estimation = estimation;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public Vector2d getEstimation() {
		return estimation;
	}
	
	public void setEstimation(Vector2d estimation) {
		this.estimation = estimation;
	}
}
