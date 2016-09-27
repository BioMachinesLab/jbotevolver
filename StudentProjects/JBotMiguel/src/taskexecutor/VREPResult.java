package taskexecutor;

import result.Result;

public class VREPResult extends Result{
	
	private float[] values;
	
	public VREPResult(int taskId, float[] values) {
		super(taskId);
		this.values = values;
	}
	
	public float[] getValues() {
		return values;
	}

}
