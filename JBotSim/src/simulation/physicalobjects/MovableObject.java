package simulation.physicalobjects;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.collisionhandling.knotsandbolts.Shape;
import simulation.util.Arguments;

public class MovableObject extends PhysicalObject {
	private static final double NUMBER_OF_CYCLES_PER_SECOND = 10;
	public static final double  MAXIMUMSPEED      = 1000;
	public static final double  TWICEMAXIMUMSPEEDPERTIMESTEP = 2.0 * MAXIMUMSPEED / NUMBER_OF_CYCLES_PER_SECOND;
	protected Environment env;
	protected Vector2d previousPosition;
	
	public MovableObject(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.env = simulator.getEnvironment();
		this.previousPosition = position;
	}
	
	public MovableObject(Simulator simulator, String name, double x, double y, double orientation, double mass, PhysicalObjectType type) {
		super(simulator, name, x, y, orientation, mass, type);
		this.env = simulator.getEnvironment();
	}
	
	public MovableObject(Simulator simulator, String name, double x, double y, double orientation, double mass, PhysicalObjectType type, Shape shape) {
		super(simulator, name, x, y, orientation, mass, type, shape);
		this.env = simulator.getEnvironment();
	}

	public void teleportTo(Vector2d position){
		setPosition(position);
		env.addTeleported(this);
	}
	
	public void move(Vector2d relativePosition) {
		position.x += relativePosition.x;
		position.y += relativePosition.y;
	}
		
	public void moveTo(Vector2d position) {
		this.position.set(position);
		this.previousPosition = position;
	}
	
	public Vector2d getPreviousPosition() {
		return previousPosition;
	}
}
