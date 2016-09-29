package taskexecutor;

import result.Result;

public class VRepResult extends Result{
	
	private float[] values;
	
	public VRepResult(int taskId, float[] values) {
		super(taskId);
		this.values = values;
	}
	
	public float[] getValues() {
		return values;
	}

}
