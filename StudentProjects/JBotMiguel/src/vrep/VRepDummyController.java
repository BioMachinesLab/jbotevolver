package vrep;

import java.util.Scanner;

import mathutils.Vector2d;

public class VRepDummyController extends VRepController {
	
	protected float[] locomotionParameters;
	
	public VRepDummyController(float[] parameters) {
		super(parameters);
		
		int ignoreValues = 4;//type, nParams, inputs, outputs
		
		locomotionParameters = new float[parameters.length-ignoreValues];
		
		for(int i = 0 ; i < locomotionParameters.length ; i++)
			locomotionParameters[i] = parameters[i+ignoreValues];
	}

	@Override
	public float[] controlStep(float[] inputs) {
		return locomotionParameters;
	}
}