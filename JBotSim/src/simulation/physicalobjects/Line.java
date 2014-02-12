package simulation.physicalobjects;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall.Edge;

public class Line extends PhysicalObject{

	private Vector2d pointA;
	private Vector2d pointB;
	
	public Line(Simulator simulator, String name, double x0, double y0, double x1, double y1) {
		super(simulator, name, x0+(x1-x0)/2, y0+(y1-y0)/2, 0, 0, PhysicalObjectType.LINE);
		pointA = new Vector2d(x0, y0);
		pointB = new Vector2d(x1, y1);
	}
	
	public Vector2d getPointA() {
		return pointA;
	}
	
	public Vector2d getPointB() {
		return pointB;
	}
	
	public Vector2d intersectsWithLineSegment(Vector2d p1, Vector2d p2) {
		Vector2d closestPoint      = null;
		Vector2d lineSegmentVector = new Vector2d(p2);
		lineSegmentVector.sub(p1);
		
		closestPoint = MathUtils.intersectLines(p1, p2, pointA, pointB);
		
		return closestPoint;
	}
	
	@Override
	public double getDistanceBetween(Vector2d fromPoint) {
		
		Vector2d light = new Vector2d(position);
		lightDirection.set(light.getX()-fromPoint.getX(),light.getY()-fromPoint.getY());

		Vector2d intersection = intersectsWithLineSegment(lightDirection,fromPoint);
		if(intersection != null) {
			return intersection.length();
		}
		return fromPoint.distanceTo(position);
	}

}