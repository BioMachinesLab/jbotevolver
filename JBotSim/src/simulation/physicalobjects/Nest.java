package simulation.physicalobjects;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class Nest extends PhysicalObject {
	
	public Nest(Simulator simulator, String name, double x, double y, double radius) {
		super(simulator, name, x, y, 0, 0, PhysicalObjectType.NEST);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, x, y, 2 * radius, radius);
	}
}