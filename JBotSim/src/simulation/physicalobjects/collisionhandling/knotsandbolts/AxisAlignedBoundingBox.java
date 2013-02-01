package simulation.physicalobjects.collisionhandling.knotsandbolts;

import java.io.Serializable;

import mathutils.Point2d;


/******************************************************************************/
/******************************************************************************/
// This is an axis aligned bounding box (AABB) class. A standard thing for doing
// collision detection. From the interface you can set the center and the size of
// the bounding box, but internally we just keep track of the corner coordinates
// of the box, because that speeds up the collision detection a fair bit.
/******************************************************************************/
/******************************************************************************/
public class AxisAlignedBoundingBox implements Serializable {

	private double x1;
	private double x2;
	private double y1;
	private double y2;

	public AxisAlignedBoundingBox(double centerX, double centerY){
		reset(centerX, centerY);
	}

	public void moveTo(double x, double y){
		x1 = x - (x2 - x1) / 2;
		x2 = x + (x2 - x1) / 2;

		y1 = y - (y2 - y1) / 2;
		y2 = y + (y2 - y1) / 2;   
	}

	public void add(AxisAlignedBoundingBox aabb)	{
		if (aabb.x1 < x1)
			x1 = aabb.x1;

		if (aabb.x2 > x2)
			x2 = aabb.x2;

		if (aabb.y1 < y1)
			y1 = aabb.y1;

		if (aabb.y2 > y2)
			y2 = aabb.y2;
	}

	public boolean overlaps(AxisAlignedBoundingBox aabb){
		//	    printf("me: x1:%f x2:%f y1:%f y2:%f\n", this.m_fX1, this.m_fX2, this.m_fY1, this.m_fY2); 
		//	    printf("ot: x1:%f x2:%f y1:%f y2:%f\n", pcAABB.m_fX1, pcAABB.m_fX2, pcAABB.m_fY1, pcAABB.m_fY2); 

		if (this.x1 <= aabb.x2)
			if (this.x2 >= aabb.x1)
				if (this.y1 <= aabb.y2)
					if (this.y2 >= aabb.y1)
						return true;
		return false;
	}

	public void reset(double centerX, double centerY) {
		x1 = x2 = centerX;
		y1 = y2 = centerY;
	}

	public void reset(double centerX, double centerY, 
			double sizeX, double sizeY) {
		x1 = centerX - sizeX / 2;
		x2 = centerX + sizeX / 2;

		y1 = centerY - sizeY / 2;
		y2 = centerY + sizeY / 2;
	}

	public void getCorners(Point2d p1, Point2d p2){
		p1.set(x1, y1);
		p2.set(x2, y2);
	}

}
