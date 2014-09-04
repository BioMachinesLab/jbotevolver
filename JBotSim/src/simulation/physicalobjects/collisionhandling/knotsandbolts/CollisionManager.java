package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.io.Serializable;
import java.util.LinkedList;

import net.jafama.FastMath;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;


public abstract class CollisionManager implements Serializable {
	
	protected Simulator simulator;
	
	
	public CollisionManager(Simulator simulator) {
		super();
		this.simulator = simulator;
	}

	public abstract void handleCollisions(Environment environment, double time);
	
	static public boolean checkCollisionBetweenTwoObjects(
				Shape collisionObject1,
				Shape collisionObject2) {

		// Check if the objects are enabled:
		if (!collisionObject1.isEnabled() || !collisionObject2.isEnabled() || collisionObject1 == collisionObject2)
			return false;

		// Check if the AABBs overlap:
		if (!collisionObject1.getAABB().overlaps(collisionObject2.getAABB()))
			return false;

		CompoundShape  compoundCollisionObject;

		// Handle compound collision objects:
		if (collisionObject1.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_COMPOUND) {
			compoundCollisionObject = (CompoundShape) collisionObject1;
			LinkedList<CompoundShape> pvecCollisionChildren = compoundCollisionObject.getCollisionChildren();

			boolean bCollisionFound = false;
			for(Shape i:pvecCollisionChildren) {
				if(checkCollisionBetweenTwoObjects(i, collisionObject2)) {
					break;
				}                                                              
			}
			return bCollisionFound;
		} else if (collisionObject2.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_COMPOUND) {
			compoundCollisionObject = (CompoundShape) collisionObject2;
			LinkedList<CompoundShape> pvecCollisionChildren = compoundCollisionObject.getCollisionChildren();

			boolean collisionFound = false;
			for(Shape i:pvecCollisionChildren) {
				if(checkCollisionBetweenTwoObjects(collisionObject1, i)) {
					break;
				}                                                              
			}

			return collisionFound;
			// Handle circle/circle collisions:
		} else if (collisionObject1.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_CIRCLE && 
				collisionObject2.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_CIRCLE) {
			
			double fMaxCollisionDistance = ((CircularShape) collisionObject1).getRadius() +
			((CircularShape) collisionObject2).getRadius();

			Vector2d distance = collisionObject1.getPosition();
			distance.subFrom(collisionObject2.getPosition());

			if (distance.length() < fMaxCollisionDistance)
			{
				collisionObject1.setCollidedWith(collisionObject2);
				return true;
			} else {
				return false;
			}
			// Handle circle/rectangle collisions:
		} else if ((collisionObject1.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_CIRCLE && 
				    collisionObject2.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_RECTANGLE)
				   || (collisionObject1.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_RECTANGLE && 
					   collisionObject2.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_CIRCLE)) {

			RectangularShape rectangle;
			CircularShape    circle;

			// Figure out which one of the objects is the circle and which is the rectangle:
			if (collisionObject1.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_CIRCLE) {
				rectangle = (RectangularShape) collisionObject2;
				circle    = (CircularShape)    collisionObject1;
				collisionObject1.setCollidedWith(rectangle);
			} else {
				rectangle = (RectangularShape) collisionObject1;
				circle    = (CircularShape)    collisionObject2;
				collisionObject1.setCollidedWith(circle);
			}
			// Move and rotate the center of the circle with respect to the pos and rot of the
			// rectangle:
			Vector2d translatedCircleCenter = circle.getPosition();
			Vector2d rectanglePosition      = rectangle.getPosition();
			translatedCircleCenter.sub(rectanglePosition);

			double rectangleRotation = rectangle.getOrientation();

			translatedCircleCenter.rotate(-rectangleRotation);

			// Move everything to the first quadrant:
			translatedCircleCenter.setX(Math.abs(translatedCircleCenter.getX()));
			translatedCircleCenter.setY(Math.abs(translatedCircleCenter.getY()));

			double halfSizeX = rectangle.getHalfSizeX();
			double halfSizeY = rectangle.getHalfSizeY();

			// Check if anything touches anything:
			if ((translatedCircleCenter.getX() - circle.getRadius() > halfSizeX) || 
					(translatedCircleCenter.getY() - circle.getRadius() > halfSizeY))
			{
				return false;
			} else {
				// Check if the circle overlaps with any of the sides of the rectangle:
				if (translatedCircleCenter.getX() < halfSizeX || translatedCircleCenter.getY() < halfSizeY)
				{
					return true;
				} else {
					// Check if the circle overlaps with a corner:
					double distanceFromRectangleCorner = translatedCircleCenter.getX() - halfSizeX;
					double temp = translatedCircleCenter.getY() - halfSizeY;

					distanceFromRectangleCorner = FastMath.sqrtQuick(distanceFromRectangleCorner * distanceFromRectangleCorner + temp * temp);
					if (distanceFromRectangleCorner < circle.getRadius()) {
						return distanceFromRectangleCorner < circle.getRadius();
					}
				}
			}
		} else if (collisionObject1.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_RECTANGLE && 
  				   collisionObject2.getCollisionObjectType() == Shape.COLLISION_OBJECT_TYPE_RECTANGLE) {
			RectangularShape pcRectangle1 = (RectangularShape) collisionObject1;
			RectangularShape pcRectangle2 = (RectangularShape) collisionObject2; 

			boolean bCollision = pcRectangle1.checkCollisionWithRectangle(pcRectangle2);
			if (bCollision)
			{
				collisionObject1.setCollidedWith(collisionObject2);
			}
			return bCollision;
		}
		return false;
	}
}
