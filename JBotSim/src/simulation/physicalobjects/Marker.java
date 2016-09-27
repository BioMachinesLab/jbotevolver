package simulation.physicalobjects;

import java.awt.Color;

import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;

public class Marker extends PhysicalObject{
	
	private Color color = Color.RED;
	private double length;
	private double radius;
	private boolean square = false;

	public Marker(Simulator simulator,  String name, double x, double y, double orientation, double radius, double length, Color color) {
		super(simulator, name, x, y, orientation, 0, PhysicalObjectType.MARKER);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, 0, 0, radius*2, radius);
		this.length = length;
		this.radius = radius;
		this.color = color;
	}
	
	public Marker(Simulator simulator,  String name, double x, double y, double orientation, double radius, double length, Color color, boolean square) {
		this(simulator, name, x, y, orientation, radius, length, color);
		this.square = square;
	}
	
	public boolean isSquare() {
		return square;
	}
	
	public Color getColor() {
		return color;
	}
	
	public double getLength() {
		return length;
	}

}
