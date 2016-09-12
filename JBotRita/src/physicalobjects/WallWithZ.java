package physicalobjects;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;

public class WallWithZ extends Wall {
	private double z;

	public WallWithZ(Simulator simulator, double x, double y, double width,
			double height, double z) {
		super(simulator, x, y, width, height);
		this.z=z;
	}
	
	public double getHeightZ(){
		return z;
	}
	
}
