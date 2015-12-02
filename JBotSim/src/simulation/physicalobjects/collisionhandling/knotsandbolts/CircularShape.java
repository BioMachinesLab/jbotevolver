package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.awt.geom.Ellipse2D;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;

public class CircularShape extends Shape {

	private  double radius;
	private  double diameter;
	private Ellipse2D.Double circle;

	public CircularShape(Simulator simulator, String name, PhysicalObject parent,
			double relativePosX, double relativePosY, double range, double radius) { 
		super(simulator, name, parent, relativePosX, relativePosY, range);
		this.radius   = radius;
		this.diameter = radius * 2.0;
		computeNewPositionAndOrientationFromParent();
	}

	public void computeNewPositionAndOrientationFromParent()
	{
		setPosition(parent.getPosition().getX() + relativePosition.getX(),
					parent.getPosition().getY() + relativePosition.getY());

		aabb.reset(parent.getPosition().getX() + relativePosition.getX(), 
				parent.getPosition().getY() + relativePosition.getY(), 
				radius * 2, radius * 2);
		
		//mult by 10000 because in the Polygon shape we do it too, since it has to be defined by an integer
		circle = getEllipse2D(parent.getPosition(), relativePosition, radius);
	}

	@Override
	public int getCollisionObjectType()
	{
		return COLLISION_OBJECT_TYPE_CIRCLE;
	}

	public double getRadius()
	{
		return radius;
	}

	public double getDiameter() {
		return diameter;
	}
	
	public Ellipse2D getCircle() {
		return circle;
	}
	
	public static Ellipse2D.Double getEllipse2D(Vector2d parent, Vector2d relativePosition, double radius) {
		return new Ellipse2D.Double(
				(parent.getX() + relativePosition.getX()-radius)*10000, 
				(parent.getY() + relativePosition.getY()-radius)*10000, 
				(radius*2)*10000, 
				(radius*2)*10000);
	}
}