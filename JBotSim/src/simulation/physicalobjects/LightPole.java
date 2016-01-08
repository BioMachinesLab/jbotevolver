package simulation.physicalobjects;


import java.awt.Color;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class LightPole extends PhysicalObject {
	
	boolean turnedOn = true;
	private Color color = Color.ORANGE;

	public LightPole(Simulator simulator,  String name, double x, double y, double radius) {
		super(simulator, name, x, y, 0, 0, PhysicalObjectType.LIGHTPOLE);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, 0, 0, 2 * radius, radius);
	}
	
	public boolean isTurnedOn() {
		return turnedOn;
	}

	public void setTurnedOn(boolean turnedOn) {
		this.turnedOn = turnedOn;
	}
	
	public void setColor(Color c) {
		this.color = c;
	}
	
	public Color getColor() {
		return color;
	}
	
}
