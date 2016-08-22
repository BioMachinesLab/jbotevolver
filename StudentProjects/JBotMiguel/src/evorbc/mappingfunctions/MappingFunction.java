package evorbc.mappingfunctions;

import mathutils.Vector2d;

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
	
}
