package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.awt.geom.Area;
import java.io.Serializable;
import java.util.ArrayList;

import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.physicalobjects.checkers.AllowOrderedLightChecker;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.physicalobjects.checkers.AllowOrderedRobotsChecker;
import simulation.physicalobjects.checkers.AllowWallChecker;


public abstract class Shape implements Serializable {
	public static final int COLLISION_OBJECT_TYPE_COMPOUND   = 0;
	public static final int COLLISION_OBJECT_TYPE_RECTANGLE  = 1;
	public static final int COLLISION_OBJECT_TYPE_CIRCLE     = 2;
	public static final int COLLISION_OBJECT_TYPE_POLYGON  = 3;
	
	protected boolean   			enabled;
	protected PhysicalObject        parent;

	protected Vector2d          	relativePosition;
	protected Vector2d          	position = new Vector2d();
	protected double                orientation = 0;
	protected AxisAlignedBoundingBox 	aabb;

	private ArrayList<Shape> collidedWith;

	//TODO::CHECK the next line... check id (-1)
	protected ClosePhysicalObjects closeRobots;
	protected ClosePhysicalObjects closePrey;
	protected ClosePhysicalObjects closeLightPoles;
	protected ClosePhysicalObjects closeWalls;//, closeHoles;
	
	public Shape(Simulator simulator, String name, PhysicalObject parent, 
			double relativePosX, double relativePosY, double range) {  
		this.enabled 	 = true; 
		this.parent  	 = parent;
		this.aabb    	 = new AxisAlignedBoundingBox(parent == null ? relativePosX : parent.getPosition().getX(),  
					parent == null ? relativePosY : parent.getPosition().getY());
		collidedWith 	 = new ArrayList<Shape>(10);
		relativePosition = new Vector2d(relativePosX, relativePosY);
		
		closeRobots 	 = new ClosePhysicalObjects(simulator.getEnvironment(), range,new AllowAllRobotsChecker(parent.getId()));
		closePrey   	 = new ClosePhysicalObjects(simulator.getEnvironment(), range,new AllowOrderedPreyChecker(parent.getId()));
		closeLightPoles  = new ClosePhysicalObjects(simulator.getEnvironment(), range,new AllowOrderedLightChecker(parent.getId()));
		closeWalls = new ClosePhysicalObjects(simulator.getEnvironment(), range, new AllowWallChecker());
		//closeHoles = new ClosePhysicalObjects(simulator, range, new AllowHoleChecker());
		
		if (parent != null) {
			setPosition(relativePosition.getX() + parent.getPosition().getX(), 
					relativePosition.getY() + parent.getPosition().getY());
			setOrientation(parent.getOrientation());
		}
	}

	public Vector2d getPosition() {
		return position;
	}

	public double getOrientation() {
		return orientation;
	}

	public void setPosition(double x, double y) {
		position.set(x, y);
	}

	public void setPosition(Vector2d vNewPos) {
		position.set(vNewPos);
	}

	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	
	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setRelativePosition(double x, double y) {
		relativePosition.set(x,y);
	}

	public void setRelativePosition(Vector2d position) {
		relativePosition = position;
	}

	public void getRelativePosition(Point2d position) {
		position.set(relativePosition);
	}

	public Vector2d getRelativePosition() {
		return relativePosition;
	}

	public AxisAlignedBoundingBox getAABB() {
		return aabb;
	}

	public PhysicalObject getFounderParent(){
		PhysicalObject founderParent = parent;
		while(founderParent.hasParent()) {
			founderParent = founderParent.getParent();
		}
		return founderParent; 
	}

	public void setParent(PhysicalObject parent) {
		this.parent = parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public PhysicalObject getParent() {
		return parent;
	}

	public abstract int getCollisionObjectType();
	public abstract void computeNewPositionAndOrientationFromParent();                               

	
	public void setCollidedWith(Shape collidedWith) {
		this.collidedWith.add(collidedWith);
	}

	public ArrayList<Shape> getCollidedWith() {
		return collidedWith;
	}

	public void clearCollidedWith(){
		collidedWith.clear();
	}

	public ClosePhysicalObjects getCloseRobot() {
		return closeRobots;
	}

	public ClosePhysicalObjects getClosePrey() {
		return closePrey;
	}
	
	public ClosePhysicalObjects getCloseLightPole(){
		return closeLightPoles;
	}
	
	public ClosePhysicalObjects getCloseWalls(){
		return this.closeWalls;
	}
	
	/*public ClosePhysicalObjects getCloseHoles() {
		return this.closeHoles;
	}*/

}
