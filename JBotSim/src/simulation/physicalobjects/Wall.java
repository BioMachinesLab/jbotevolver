package simulation.physicalobjects;

import java.awt.Color;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.RectangularShape;

public class Wall extends PhysicalObject{

	private double width, height;

	private Edge left, right, top, bottom;
	private Edge[] edges;
	public Color color = Color.BLUE;
	
	public Wall(Simulator simulator, String name, double x, double y,
			double orientation, double mass, 
			double range, double relativeRotation, double width, double height,
			PhysicalObjectType type) {
		super(simulator, name, x, y, orientation, mass, type);
		this.width = width;
		this.height = height;
		//this.x = x;
		//this.y = y;
		this.setPosition(new Vector2d(x,y));
		this.shape = new RectangularShape(simulator, name, this, 
				0, 0, range, relativeRotation, width, height);
		initializeEdges();
		edges = getEdges();
		if(type == PhysicalObjectType.WALLBUTTON)
			color = Color.RED;
	}
	
	public Wall(Simulator simulator, double x, double y, double width, double height) {
		this(simulator, "wall"+simulator.getRandom().nextInt(1000), x, y, 0, 0, 0, 0, width, height, PhysicalObjectType.WALL);
	}
	
	public void moveWall() {
		initializeEdges();
		edges = getEdges();
	}

	private void initializeEdges() {
		Vector2d topLeft = new Vector2d(getTopLeftX(), getTopLeftY()),
				topRight = new Vector2d(getTopLeftX() + getWidth(), getTopLeftY()),
				bottomLeft = new Vector2d(getTopLeftX(), getTopLeftY() - getHeight()),
				bottomRight = new Vector2d(getTopLeftX() + getWidth(), 
						getTopLeftY() - getHeight());
		top    = new Edge(topLeft, topRight);
		right  = new Edge(topRight, bottomRight);
		left   = new Edge(bottomLeft, topLeft);
		bottom = new Edge(bottomRight, bottomLeft);
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getTopLeftX() {
		return getPosition().getX() - width/2;
	}

	public double getTopLeftY(){
		return getPosition().getY() + height/2;
	}

	public Edge[] getEdges() {
		return new Edge[]{left, right, bottom, top};
	}

	public class Edge{
		
		private Vector2d p1, p2;
		private Vector2d normal;
		
		public Edge(Vector2d p1, Vector2d p2){
			this.p1 = p1;
			this.p2 = p2;
			this.normal = new Vector2d();		
			this.normal.x = -(p2.y-p1.y);
			this.normal.y = (p2.x-p1.x);
		}

		public Vector2d getP1() {
			return p1;
		}

		public Vector2d getP2(){
			return p2;
		}
		
		public Vector2d getNormal() {
			return normal;
		}
	}
	
	public Vector2d intersectsWithLineSegment(Vector2d p1, Vector2d p2) {
		Vector2d closestPoint      = null;
		Vector2d lineSegmentVector = new Vector2d(p2);
		lineSegmentVector.sub(p1);
		
		for (Edge e : edges) {
			double dot = e.getNormal().dot(lineSegmentVector);
			if (dot < 0) {
				Vector2d e1 = e.getP1();
				Vector2d e2 = e.getP2();
				closestPoint = MathUtils.intersectLines(p1, p2, e1, e2);
				if(closestPoint != null) {
					break;
				}
			}
		}
		return closestPoint;
	}
	
	public Vector2d intersectsWithLineSegment(Vector2d p1, Vector2d p2, double maxReflectionAngle) {
		Vector2d closestPoint      = null;
		Vector2d lineSegmentVector = new Vector2d(p2);
		lineSegmentVector.sub(p1);
		
		for (Edge e : edges) {
			double dot = e.getNormal().dot(lineSegmentVector);
			if (dot < 0) {
				Vector2d e1 = e.getP1();
				Vector2d e2 = e.getP2();
				closestPoint = MathUtils.intersectLines(p1, p2, e1, e2);
				if(closestPoint != null) {
					double a = lineSegmentVector.angle(e.getNormal()) -Math.PI;
					
					if(Math.abs(a) >= maxReflectionAngle)
						closestPoint = null;
					break;
				}
			}
		}
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