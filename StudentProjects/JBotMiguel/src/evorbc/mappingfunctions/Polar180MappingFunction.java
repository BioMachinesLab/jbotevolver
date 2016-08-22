package evorbc.mappingfunctions;

import mathutils.Vector2d;

public class Polar180MappingFunction extends PolarMappingFunction{
	
	public Polar180MappingFunction(double[][][] repertoire) {
		super(repertoire);
	}

	/**
	 * This mapping function receives two values ranged from [0,1]: angle and speed.
	 * It then converts them to the following ranges: angle [-PI/2,PI/2], speed [-1,1]
	 */
	@Override
	public Vector2d map(double... args) {
		
		double angle = args[0];
		double speed = args[1];
		
		double alpha = (angle-0.5)* 2 * (Math.PI/2);
		double rho = speed*2.0 - 1.0;
		
		int x = polarX(alpha,rho);
		int y = polarY(alpha,rho);
		
		return new Vector2d(x,y);
	}

}
