package simulation.physicalobjects.collisionhandling.knotsandbolts;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;

public class CircularShape extends Shape {

	private  double radius;
	private  double diameter;

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
}