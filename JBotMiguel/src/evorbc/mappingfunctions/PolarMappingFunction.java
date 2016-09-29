package evorbc.mappingfunctions;

import mathutils.Vector2d;
import multiobjective.MOChromosome;

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
	
	@Override
	public void fill() {
    	
		double maxDist = 0;
    	
    	for(int x = 0 ; x < repertoire.length ; x++) {
    		for(int y = 0 ; y < repertoire[x].length ; y++) {
    			if(repertoire[x][y] != null) {
    				int posY = y;
    				int posX = x; 
	    			posX-= repertoire.length/2;
	        		posY-= repertoire[0].length/2;
	        		maxDist = Math.max(maxDist,new Vector2d(posX,posY).length());
	        			
    			}
    		}
    	}
    	
    	//create a copy of the map so that we only expand the map
    	//with some of the original behaviors
    	double[][][] mapNew = new double[repertoire.length][repertoire[0].length][];
    	
    	for(int x = 0 ; x < repertoire.length ; x++) {
    		for(int y = 0 ; y < repertoire[x].length ; y++) {
    			
    			double len = 0;
    			
    			len = new Vector2d(x-repertoire.length/2.0,y-repertoire[x].length/2.0).length();
    			if(len < maxDist) {
    				if(repertoire[x][y] == null) {
    					mapNew[x][y] = findNearest(x,y,repertoire);
    				}
    			}
    		}
    	}
    	
    	//merge the two maps
    	for(int i = 0 ; i < mapNew.length ; i++) {
    		for(int j = 0 ; j < mapNew[i].length ; j++) {
    			if(mapNew[i][j] != null) {
    				repertoire[i][j] = mapNew[i][j];
    			}
    		}
    	}
    }
	
}
