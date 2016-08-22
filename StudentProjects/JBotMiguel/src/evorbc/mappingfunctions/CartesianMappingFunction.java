package evorbc.mappingfunctions;

import java.util.Arrays;

import mathutils.Vector2d;

public class CartesianMappingFunction extends MappingFunction{
	
	double[] minMax = new double[]{Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
	
	public CartesianMappingFunction(double[][][] repertoire) {
		super(repertoire);
		
		for(int x = 0 ; x < repertoire.length ; x++) {
			for(int y = 0 ; y < repertoire[x].length ; y++) {
				if(repertoire[x][y] != null) {
					minMax[0] = Math.min(minMax[0],x);//min x
        			minMax[1] = Math.min(minMax[1],y);//min Y
        			minMax[2] = Math.max(minMax[2],x);//max x
        			minMax[3] = Math.max(minMax[3],y);//max Y
				}
			}
		}
	}

	/**
	 * This mapping function receives two values ranged from [0,1]: a and b.
	 * It then scales the mapping parameters according to the dimension of the repertoire, with respect to the behaviors (x,y).
	 */
	@Override
	public Vector2d map(double... args) {
		
		double a = args[0];
		double b = args[1];
		
		double x = a * (minMax[2] - minMax[0]) + minMax[0]; //a * (xMax - xMin) + xMin
		double y = b * (minMax[3] - minMax[1]) + minMax[1]; //b * (yMax - yMin) + yMin
		
		return new Vector2d(y,x);
	}

}
