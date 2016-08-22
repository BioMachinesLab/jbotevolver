package evorbc.mappingfunctions;

import mathutils.Vector2d;

public abstract class PolarMappingFunction extends MappingFunction{
	
	protected double repertoireRadius;
	
	public PolarMappingFunction(double[][][] repertoire) {
		super(repertoire);
		
		int size = repertoire.length;
		
		for(int x = 0 ; x < size ; x++) {
			for(int y = 0 ; y < size ; y++) {
				if(repertoire[x][y] != null)
					repertoireRadius = Math.max(repertoireRadius,new Vector2d(x - size/2.0, y - size/2.0).length());
			}
		}
	}
	
	protected int polarX(double alpha, double rho) {
		return (int)(rho*repertoireRadius*Math.cos(alpha) + repertoire.length/2);
	}
	
	protected int polarY(double alpha, double rho) {
		return (int)(rho*repertoireRadius*Math.sin(alpha) + repertoire[0].length/2);
	}
	
}
