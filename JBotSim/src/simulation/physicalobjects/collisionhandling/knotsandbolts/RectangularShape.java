package simulation.physicalobjects.collisionhandling.knotsandbolts;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObject;



/*******************************************************************************

The corners of the collision object are organized in the following way 
when rotation is 0:
                          ^
                          |
          X1,Y1...........|..........X2,Y1
          .               |              .
          .               |              .
         --------------------------------.
           .              |              .
           .              |              .  
          X1,Y2...........|..........X2,Y2
                          |


Not the most intuitive way, but it works and you are welcome to change it 
(changes are local).

 *******************************************************************************/


public class RectangularShape extends Shape {
	private Vector2d cornerX1Y1 = new Vector2d();
	private Vector2d cornerX2Y1 = new Vector2d();
	private Vector2d cornerX2Y2 = new Vector2d();
	private Vector2d cornerX1Y2 = new Vector2d();

	private double halfSizeX;
	private double halfSizeY;

	private double relativeRotation;


	public RectangularShape(Simulator simulator, String name, PhysicalObject parent,
			double relativePosX, double relativePosY, double range,
			double relativeRotation, double sizeX, double sizeY) {
		super (simulator, name, parent, relativePosX, relativePosY, range);
		this.relativeRotation = relativeRotation;
		this.halfSizeX = sizeX / 2;
		this.halfSizeY = sizeY / 2;

		/*if (parent != null){
			computeNewPositionAndOrientationFromParent();
		}
		else {*/
			cornerX1Y1.set( relativePosX - halfSizeX, relativePosY + halfSizeY);

			cornerX2Y1.set( relativePosX + halfSizeX, relativePosY + halfSizeY);

			cornerX2Y2.set( relativePosX + halfSizeX, relativePosY - halfSizeY);

			cornerX1Y2.set( relativePosX - halfSizeX, relativePosY - halfSizeY);
			aabb.reset(relativePosX, relativePosY, sizeX, sizeY);
			//System.out.println(super.aabb.toString());
			
			setPosition(relativePosX, relativePosY);
			setOrientation(relativeRotation);
		//}
	}

	public void computeNewPositionAndOrientationFromParent() {                               
		// First we rotate the rectangle and afterwards we move it:
		double fRotation = relativeRotation + parent.getOrientation();    
		fRotation = MathUtils.normalizeAngle(fRotation);
		setOrientation(fRotation);
		System.out.println("PARENT: " + parent.getPosition());

		// Based on the rotation of two points we can find the other two: 
		cornerX1Y1.set(-halfSizeX, -halfSizeY);

		cornerX2Y1.set(halfSizeX, -halfSizeY);

		cornerX1Y1.rotate(fRotation);
		cornerX2Y1.rotate(fRotation);

		cornerX2Y2.set( -cornerX1Y1.getX(),-cornerX1Y1.getY());

		cornerX1Y2.set( -cornerX2Y1.getX(), -cornerX2Y1.getY());

		// Calculate the size of the AABB:       
		double fAABBSizeX = Math.abs(cornerX2Y2.getX()) > Math.abs(cornerX2Y1.getX()) ? 
				Math.abs(cornerX2Y2.getX()) * 2 : Math.abs(cornerX2Y1.getX()) * 2;
		double fAABBSizeY = Math.abs(cornerX2Y2.getY()) > Math.abs(cornerX2Y1.getY()) ? 
				Math.abs(cornerX2Y2.getY()) * 2 : Math.abs(cornerX2Y1.getY()) * 2;

		// Now move the rectangle:
		Vector2d vNewPos = relativePosition;
		vNewPos.rotate(parent.getOrientation());

		vNewPos.add(parent.getPosition());
		setPosition(vNewPos);

		cornerX1Y1.add(vNewPos);
		cornerX2Y1.add(vNewPos);
		cornerX2Y2.add(vNewPos);
		cornerX1Y2.add(vNewPos);

		aabb.reset(vNewPos.getX(), vNewPos.getY(), fAABBSizeX, fAABBSizeY);
	}

	public int getCollisionObjectType()
	{
		return COLLISION_OBJECT_TYPE_RECTANGLE;
	}

	public double getHalfSizeX()
	{
		return halfSizeX;
	}

	public double getHalfSizeY()
	{
		return halfSizeY;
	}

	public boolean checkCollisionWithRectangle(RectangularShape pc_rectangle)
	{
		if (checkHalfCollisionWithRectangle(pc_rectangle))
			return true;
		else
			return pc_rectangle.checkHalfCollisionWithRectangle(this);
	}


	private boolean checkCorner(Vector2d v) {
		return (v.getX() <= halfSizeX && v.getX() >= -halfSizeX && v.getY() <= halfSizeY && v.getY() >= -halfSizeY);    
	}

	// We check if pc_rectangle has a corner within "this" rectangle:
	private boolean checkHalfCollisionWithRectangle(RectangularShape pc_rectangle)
	{
		Vector2d cornerX1Y1 = pc_rectangle.cornerX1Y1;
		Vector2d cornerX2Y1 = pc_rectangle.cornerX2Y1;
		Vector2d cornerX2Y2 = pc_rectangle.cornerX2Y2;
		Vector2d cornerX1Y2 = pc_rectangle.cornerX1Y2;      

		Vector2d myPosition = getPosition();

		cornerX1Y1.sub(myPosition);
		cornerX1Y2.sub(myPosition);
		cornerX2Y2.sub(myPosition);
		cornerX2Y1.sub(myPosition);

		cornerX1Y1.negate();
		cornerX1Y2.negate();
		cornerX2Y2.negate();
		cornerX2Y1.negate();

		double fMyRot = getOrientation();

		cornerX1Y1.rotate(-fMyRot);
		cornerX1Y2.rotate(-fMyRot);
		cornerX2Y2.rotate(-fMyRot);
		cornerX2Y1.rotate(-fMyRot);

		return checkCorner(cornerX1Y1) ||
		checkCorner(cornerX1Y2) || 
		checkCorner(cornerX2Y2) || 
		checkCorner(cornerX2Y1);
	}


	public void setSize(double sizeX, double sizeY)
	{
		halfSizeX = sizeX / 2;
		halfSizeY = sizeY / 2;

		computeNewPositionAndOrientationFromParent();
	}
}
