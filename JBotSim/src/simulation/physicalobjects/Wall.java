package simulation.physicalobjects;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.io.Serializable;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.PolygonShape;
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
		initializeEdges();
		edges = getEdges();
		
		defineShape(simulator);
		
		if(type == PhysicalObjectType.WALLBUTTON)
			color = Color.RED;
	}
	
	public Wall(Simulator simulator, double x, double y, double width, double height) {
		this(simulator, "wall"+simulator.getRandom().nextInt(1000), x, y, 0, 0, 0, 0, width, height, PhysicalObjectType.WALL);
		defineShape(simulator);
	}
	
	public Wall(Simulator simulator, Vector2d p1, Vector2d p2, double wallSize) {
		super(simulator, "wall"+simulator.getRandom().nextInt(1000), (p1.x+p2.x)/2, (p1.y+p2.y)/2, 0, 0, PhysicalObjectType.WALL);
		this.width = p1.distanceTo(p2);
		this.height = wallSize;
		initializeEdges(p1,p2);
		edges = getEdges();
		defineShape(simulator);
	}
	
	protected void defineShape(Simulator simulator) {
		
		double[] xs = new double[4];
		xs[0] = edges[3].getP1().x;
		xs[1] = edges[1].getP1().x;
		xs[2] = edges[2].getP1().x;
		xs[3] = edges[0].getP1().x;
		
		double[] ys = new double[4];
		ys[0] = edges[3].getP1().y;
		ys[1] = edges[1].getP1().y;
		ys[2] = edges[2].getP1().y;
		ys[3] = edges[0].getP1().y;
		
		this.shape = new PolygonShape(simulator, name, this, 0, 0, 0, xs, ys);
	}
	
	public void moveWall() {
		initializeEdges();
		edges = getEdges();
	}

	private void initializeEdges(Vector2d p1, Vector2d p2) {
		
		if(p1.x > p2.x) {
			Vector2d temp = p1;
			p1 = p2;
			p2 = temp;
		}
		
		double angle = Math.atan2(p1.y-p2.y,p1.x-p2.x);
		angle+=Math.PI/2;
		
		double var = this.height;
		
		Vector2d p1a = new Vector2d(p1);
		Vector2d p1b = new Vector2d(p2);
		Vector2d side = new Vector2d(var*Math.cos(angle),var*Math.sin(angle));
		
		top = new Edge(p1b,p1a);
		
		Vector2d p2a = new Vector2d(p1b);
		Vector2d p2b = new Vector2d(p1b);
		p2b.add(side);
		
		right = new Edge(p2b,p2a);
		
		Vector2d p3a = new Vector2d(p2b);
		Vector2d p3b = new Vector2d(p1a);
		p3b.add(side);
		
		bottom = new Edge(p3b, p3a);

		Vector2d p4a = new Vector2d(p3b);
		Vector2d p4b = new Vector2d(p3b);
		p4b.sub(side);
		left = new Edge(p4b,p4a);
		
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

	public class Edge implements Serializable{
		
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
//			double dot = e.getNormal().dot(lineSegmentVector);
//			if (dot < 0) {
				Vector2d e1 = e.getP1();
				Vector2d e2 = e.getP2();
				Vector2d intersection = MathUtils.intersectLines(p1, p2, e1, e2);
				if(intersection != null) {

//					double a = lineSegmentVector.angle(e.getNormal()) -Math.PI;
					
//					if(Math.abs(a) >= maxReflectionAngle)
//						intersection = null;
					
					if(closestPoint != null) {
						if(intersection.distanceTo(p1) < closestPoint.distanceTo(p1))
							closestPoint = intersection;
					} else {
						closestPoint = intersection;
					}
//					break;
//				}
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