package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.awt.Polygon;
import java.awt.geom.Area;
import simulation.Simulator;
import simulation.physicalobjects.*;

public class PolygonShape extends Shape {
	
	private double[] x;
	private double[] y;
	private Polygon polygon;
	public boolean collision = false;

	public PolygonShape(Simulator simulator, String name, PhysicalObject parent,
			double relativePosX, double relativePosY, double range, double[] x, double[] y) {
		super (simulator, name, parent, relativePosX, relativePosY, range);

		this.x = x;
		this.y = y;
		
		computeNewPositionAndOrientationFromParent();
	}
	
	@Override
	public void computeNewPositionAndOrientationFromParent() {
		int[] xi = new int[x.length];
		int[] yi = new int[y.length];
		
		for(int i = 0 ; i < x.length ; i++) {
			xi[i] = (int)(x[i]*10000);
			yi[i] = (int)(y[i]*10000);
		}
		
		this.polygon = new Polygon(xi, yi, xi.length);
	}

	public int getCollisionObjectType(){
		return COLLISION_OBJECT_TYPE_RECTANGLE;
	}
	
	public Polygon getPolygon() {
		return polygon;
	}
	
	public boolean checkCollisionWithShape(java.awt.Shape other) {
		Area areaA = new Area(this.polygon);
		areaA.intersect(new Area(other));
		
		if(!areaA.isEmpty()) { 
			collision = true;
		}
	
		return !areaA.isEmpty();
	}
	
	public boolean checkCollisionWithShape(Shape other){
		if(other instanceof CircularShape)
			return this.checkCollisionWithShape(((CircularShape)other).getCircle());
		return false;
	}
	
}
