package evorbc.mappingfunctions;

import mathutils.Vector2d;
import multiobjective.MOChromosome;

public abstract class MappingFunction {
	
	protected double[][][] repertoire;
	
	public MappingFunction(double[][][] repertoire) {
		this.repertoire = repertoire;
	}
	
	/**
	 * Responsible for selecting a particular primitive from the repertoire,
	 * depending on the parameters received from the controller.
	 * 
	 * @param args mapping function parameters, ranged from [0,1]
	 * @return the location of the selected primitive (Vector2d)
	 */
	public abstract Vector2d map(double...args);
	
	public abstract void fill();
	
	protected double[] findNearest(int x, int y, double[][][] map) {
    	
    	double[] nearest = null;
    	double nearestDistance = Double.MAX_VALUE;
    	Vector2d refPos = new Vector2d(x,y);
    	
    	for(int i = 0 ; i < map.length ; i++) {
    		for(int j = 0 ; j < map[i].length ; j++) {
    			if(map[i][j] != null) {
    				double dist = new Vector2d(i,j).distanceTo(refPos);
//    				double fitness = getFitness(map[i][j]);
    				if(/*fitness >= prune && */dist < nearestDistance) {
    					nearestDistance = dist;
    					nearest = map[i][j];
    				}
    			}
    		}
    	}
    	return nearest;
    }
	
	public static void prune(double[][][] repertoire){
		//simplest pruning method is to remove behaviors with a low number of neighbours
		
		boolean removed;
		
		do {
			removed = false;
			
			for(int x = 0 ; x < repertoire.length ; x++) {
				for(int y = 0 ; y < repertoire[x].length ; y++) {
					
					if(repertoire[x][y] != null) {
						
						int neighbours = countNeighbours(repertoire, x,y);
						
						if(neighbours <= 2) {
							removed = true;
							repertoire[x][y] = null;
						}
					}
				}
			}
		}while(removed);
	}
	
	private static int countNeighbours(double[][][] repertoire, int x, int y) {
		int count = 0;
		
		for(int xi = x-1 ; xi <= x+1 ; xi++) {
			for(int yi = y-1 ; yi <= y+1 ; yi++) {
				if(xi >= 0 && yi >= 0 && xi < repertoire.length && yi < repertoire[xi].length && (xi != x || yi != y)) {
					if(repertoire[xi][yi] != null) {
						count++;
					}
				}
			}
		}
		return count;
	}
	
	public static int countBehaviors(double[][][] repertoire) {
		int count = 0;
		for(int x = 0 ; x < repertoire.length ; x++) {
			for(int y = 0 ; y < repertoire[x].length ; y++) {
				count+=repertoire[x][y] != null ? 1 : 0;
			}
		}
		return count;
	}
	
	public static void printRepertoire(double[][][] repertoire, int dx, int dy) {
    	for(int x = 0 ; x < repertoire.length ; x++) {
			System.out.println();
			for(int y = 0 ; y < repertoire[x].length ; y++) {
				
				if (dx == x && dy == y) {
					System.out.print("#");
					continue;
				}
				
				if(repertoire[x][y] == null)
					System.out.print(" ");
				else
					System.out.print("X");
			}
		}
    }
	
}
