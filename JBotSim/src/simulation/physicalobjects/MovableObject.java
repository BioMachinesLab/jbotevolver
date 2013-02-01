package simulation.physicalobjects;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.collisionhandling.knotsandbolts.Shape;

public class MovableObject extends PhysicalObject {
	private static final double NUMBER_OF_CYCLES_PER_SECOND = 10;
	public static final double  MAXIMUMSPEED      = 0.50;
	public static final double  TWICEMAXIMUMSPEEDPERTIMESTEP = 2.0 * MAXIMUMSPEED / NUMBER_OF_CYCLES_PER_SECOND;
	protected Environment env;
	
	public MovableObject(Simulator simulator, String name, double x, double y, 
			double orientation, double mass, PhysicalObjectType type, Shape shape) {
		super(simulator, name, x, y, orientation, mass, type);
		this.shape = shape;
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
	}
}
