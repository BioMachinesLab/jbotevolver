package simulation.physicalobjects;

import java.awt.Color;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;
import simulation.robot.Robot;

public class Prey extends MovableObject {

	private Robot holder;
	private Color color;

	public Prey(Simulator simulator,  String name, double x, double y, double angle, double mass, double radius) {
		super(simulator,name, x, y, angle, mass, PhysicalObjectType.PREY, null);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, 0, 0, 2 * radius, radius);
	}
	
	public Prey(Simulator simulator, String name, Vector2d position, int angle,
			double mass, double radius) {
		this(simulator, name, position.x, position.y, angle, mass, radius);
		color = Color.CYAN;
	}

	
	public void setCarrier(Robot robot) {
		holder=robot;
		if(robot==null){
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
	
	public Robot getHolder(){
		return holder;
	}
	
	@Override
	public void teleportTo(Vector2d position) {
		super.teleportTo(position);
	}


	@Override
	public Vector2d getPosition() {
		if (holder==null){
			return super.getPosition();
		} else {
			return holder.getPosition();
		}
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public String toString() {		
		return "Prey [holder=" + holder + ", position=" + position + "]";
	}

//	public boolean isEnabled() {
//		return holder==null;
//	}


}
