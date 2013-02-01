package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.util.LinkedList;

import mathutils.Vector2d;


import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;

public class CompoundShape extends Shape{

	private LinkedList<CompoundShape> children = new LinkedList<CompoundShape>();

	public CompoundShape(Simulator simulator, String name, PhysicalObject parent, 
			double relativePosX, double relativePosY, double range){
		super(simulator,name, parent, relativePosX, relativePosY, range);
	}

	public void computeNewPositionAndOrientationFromParent() {
		if (parent != null) {
			Vector2d newPosition = relativePosition;
			newPosition.rotate(parent.getOrientation());
			newPosition.add(parent.getPosition());
			setPosition(newPosition);
			setOrientation(parent.getOrientation());

			aabb.reset(newPosition.getX(), newPosition.getY(), 0, 0);

			for (CompoundShape child : children) {
				if (child.isEnabled()) {
					child.computeNewPositionAndOrientationFromParent();
					aabb.add(child.getAABB());
				}
			}
		} else {
			Vector2d position = children.get(0).getPosition();

			setPosition(position);
			setOrientation(0);

			aabb.reset(position.getX(), position.getY(), 0, 0);

			for (Shape child : children) {
				if (child.isEnabled()) {
					aabb.add(child.getAABB());
				}
			}
		}
	}

	public void addCollisionChild(CompoundShape newChild) {
		children.addLast(newChild);
	}

	public int getCollisionObjectType() {
		return COLLISION_OBJECT_TYPE_COMPOUND;
	}

	public LinkedList<CompoundShape> getCollisionChildren() {
		return children;
	}

	public void deleteChildren() {
		children.clear();
	}
}
