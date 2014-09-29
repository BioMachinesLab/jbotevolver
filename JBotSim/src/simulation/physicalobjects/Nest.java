package simulation.physicalobjects;

import java.awt.Color;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class Nest extends PhysicalObject {
	
	private Color color;
	
	public Nest(Simulator simulator, String name, double x, double y, double radius) {
		super(simulator, name, x, y, 0, 0, PhysicalObjectType.NEST);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, 0, 0, 2 * radius, radius);
		color = Color.LIGHT_GRAY;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
}