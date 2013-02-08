package simulation.physicalobjects;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.physicalobjects.collisionhandling.knotsandbolts.Shape;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;
import simulation.util.Arguments;

public class PhysicalObject extends SimulatorObject implements
		Comparable<PhysicalObject> {

	protected Vector2d position = new Vector2d();
	protected double mass;
	protected double orientation;
	private int id;
	private boolean involvedInCollison = false;
	private PhysicalObjectType type;
	public Shape shape = null;

	private boolean enabled = true;

	public PhysicalObject(Simulator simulator, Arguments args) {
		super(args.getArgumentAsStringOrSetDefault("name", "defaultName"));
		double x = args.getArgumentAsDouble("x");
		double y = args.getArgumentAsDouble("y");
		this.position.set(x,y);
		
		orientation = Math.toRadians(args.getArgumentAsDouble("orientation"));
		mass = args.getArgumentAsDouble("mass");
		type = PhysicalObjectType.valueOf(args.getArgumentAsStringOrSetDefault("type","ROBOT").toUpperCase());
		
		this.id = simulator.getAndIncrementNumberPhysicalObjects(type);
	}
	
	public PhysicalObject(Simulator simulator, String name, double x, double y, double orientation, double mass, PhysicalObjectType type) {
		super(name);
		position.set(x, y);

		this.orientation = orientation;
		this.mass = mass;
		this.type = type;

		this.id = simulator.getAndIncrementNumberPhysicalObjects(type);
	}
	
	public PhysicalObject(Simulator simulator, String name, double x, double y, double orientation, double mass, PhysicalObjectType type, Shape shape) {
		this(simulator, name, x, y, orientation, mass, type);
		this.shape = shape;
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

	public boolean hasParent() {
		return false;
	}

	public PhysicalObject getParent() {
		return null;
	}

	public void setMass(double f_mass) {
		mass = f_mass;
	}

	public double getMass() {
		return mass;
	}

	public int getId() {
		return id;
	}

	public PhysicalObjectType getType() {
		return type;
	}

	// @Override
	public int compareTo(PhysicalObject o) {
		return o.id - id;
	}

	@Override
	public String toString() {
		return "id=" + id + ", type=" + type + "]";
	}

	public double getRadius() {
		return ((CircularShape) shape).getRadius();
	}

	public double getDiameter() {
		return 2 * getRadius();
	}

	public boolean isInvolvedInCollison() {
		return involvedInCollison;
	}

	public void setInvolvedInCollison(boolean involvedInCollison) {
		this.involvedInCollison = involvedInCollison;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}