package simulation.physicalobjects;


import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class LightPole extends PhysicalObject {
	
	boolean turnedOn = true;

	public LightPole(Simulator simulator,  String name, double x, double y, double radius) {
		super(simulator, name, x, y, 0, 0, PhysicalObjectType.LIGHTPOLE);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, x, y, 2 * radius, radius);
	}
	
	public Vector2d getPosition() {
		return super.getPosition();
	}
	
	public boolean isTurnedOn() {
		return turnedOn;
	}

	public void setTurnedOn(boolean turnedOn) {
		this.turnedOn = turnedOn;
	}
	
}
