package evorbc.mappingfunctions;

import mathutils.Vector2d;
import multiobjective.MOChromosome;

public class Polar360MappingFunction extends PolarMappingFunction{
	
	public Polar360MappingFunction(double[][][]repertoire) {
		super(repertoire);
	}

	/**
	 * This mapping function receives two values ranged from [0,1]: angle and speed.
	 * It then converts them to the following ranges: alpha [-180º,180º], rho [0,1]
	 */
	@Override
	public Vector2d map(double... args) {
		double angle = args[0];
		double speed = args[1];
		
		double alpha = (angle*2.0 - 1.0) * Math.PI;
		double rho = speed;
		
		int x = polarX(alpha,rho);
		int y = polarY(alpha,rho);
		
		return new Vector2d(x,y);
	}

}
