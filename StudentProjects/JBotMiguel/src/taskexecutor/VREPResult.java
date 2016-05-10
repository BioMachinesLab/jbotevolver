package taskexecutor;

import result.Result;

public class VREPResult extends Result{
	
	private float[] values;
	
	public VREPResult(float[] values) {
		this.values = values;
	}
	
	public float[] getValues() {
		return values;
	}

}
