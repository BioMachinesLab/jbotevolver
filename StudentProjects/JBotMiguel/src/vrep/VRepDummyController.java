package vrep;

import java.util.Scanner;

import mathutils.Vector2d;

public class VRepDummyController extends VRepController {
	
	protected float[] locomotionParameters;
	
	public VRepDummyController(float[] parameters) {
		super(parameters);
		locomotionParameters = new float[parameters.length-1];
		
		for(int i = 0 ; i < locomotionParameters.length ; i++)
			locomotionParameters[i] = parameters[i+1];
	}

	@Override
	public float[] controlStep(float[] inputs) {
		return locomotionParameters;
	}
}