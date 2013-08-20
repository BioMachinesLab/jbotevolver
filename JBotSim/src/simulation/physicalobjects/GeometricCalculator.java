package simulation.physicalobjects;

import java.io.Serializable;

import mathutils.Vector2d;

public class GeometricCalculator implements Serializable {

	//to avoid constant allocation, used as temporary variable for calculations
	private Vector2d lightDirection = new Vector2d();

	public GeometricInfo getGeometricInfoBetween(PhysicalObject fromObject,
			PhysicalObject toObject, double time) {
		Vector2d coord = fromObject.position;
		Vector2d light = toObject.position;
		lightDirection.set(light.getX()-coord.getX(),light.getY()-coord.getY());
		double lightAngle=fromObject.getOrientation()-lightDirection.getAngle();

		if(lightAngle>Math.PI){
			lightAngle-=2*Math.PI;
		} else if(lightAngle<-Math.PI){ 
			lightAngle+=2*Math.PI;
		}
		return new GeometricInfo(lightAngle, lightDirection.length());
	}

	public GeometricInfo getGeometricInfoBetween(Vector2d fromPoint, double orientation,
			PhysicalObject toObject, double time) {
		Vector2d light = toObject.position;
		lightDirection.set(light.getX()-fromPoint.getX(),light.getY()-fromPoint.getY());
		double lightAngle=orientation-lightDirection.getAngle();

		if(lightAngle>Math.PI){
			lightAngle-=2*Math.PI;
		} else if(lightAngle<-Math.PI){ 
			lightAngle+=2*Math.PI;
		}
		return new GeometricInfo(lightAngle, lightDirection.length());
	}
	
	public GeometricInfo getGeometricInfoBetweenPoints(Vector2d fromPoint, double orientation,
			Vector2d toPoint, double time){
		Vector2d light = toPoint;
		lightDirection.set(light.getX()-fromPoint.getX(),light.getY()-fromPoint.getY());
		double lightAngle=orientation-lightDirection.getAngle();

		if(lightAngle>Math.PI){
			lightAngle-=2*Math.PI;
		} else if(lightAngle<-Math.PI){ 
			lightAngle+=2*Math.PI;
		}
		return new GeometricInfo(lightAngle, lightDirection.length());
	}

	public double getDistanceBetween(Vector2d fromPoint, PhysicalObject toObject, double time) {
		return toObject.getDistanceBetween(fromPoint);
	}
}