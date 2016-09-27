package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.awt.Polygon;
import java.awt.geom.Area;

import simulation.Simulator;
import simulation.physicalobjects.*;
import tests.Cronometer;

public class PolygonShape extends Shape {
	
	private double[] x;
	private double[] y;
	private Polygon polygon;
	public boolean collision = false;
	private Area area;

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
		area = new Area(this.polygon);
	}

	public int getCollisionObjectType(){
		return COLLISION_OBJECT_TYPE_RECTANGLE;
	}
	
	public Polygon getPolygon() {
		return polygon;
	}
	
}
