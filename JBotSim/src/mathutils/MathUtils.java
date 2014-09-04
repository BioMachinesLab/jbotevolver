package mathutils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import net.jafama.FastMath;

public class MathUtils implements Serializable {

	public static double modPI(double angle) {
		while (angle < 0)
			angle += 2.0 * Math.PI;

		while (angle > 2.0 * Math.PI)
			angle -= 2.0 * Math.PI;

		return angle;
	}

	public static boolean angleInRange(double angle, double min, double max) {
		if (min < max) {
			return (angle > min && angle < max);
		}

		return (angle > min || angle < max);
	}

	public static double modPI2(double angle) {
		while (angle < -Math.PI)
			angle += 2.0 * Math.PI;

		while (angle > Math.PI)
			angle -= 2.0 * Math.PI;

		return angle;
	}

	// Returns the normalized angle in the range [0,2*Math.PI)
	public static double normalizeAngle(double ang) {
		return (ang < 0 ? ang % 2 * Math.PI + 2 * Math.PI : ang % 2 * Math.PI);
	}

	// Returns the normalized angle in the range [-PI,PI)
	public static double normalizeAngleNegativePIPositivePI(double ang) {
		return (normalizeAngle(ang) > Math.PI ? normalizeAngle(ang) - 2
				* Math.PI : normalizeAngle(ang));
	}

	//
	// public static boolean rangeBoundLess( dRangeBound l1, dRangeBound l2 ) {
	// return (l1.angle < l2.angle );
	// }

	// compute the cross-correlations of two series
	public static double crossCorrelation(double[] vec_first,
			double[] vec_second, int n_num_steps) {
		if (n_num_steps == 0)
			return 0.0;

		// compute the correlation series with null delay
		double fCCNumeratorSum = 0.0;
		double fCCDenominatorSumFirst = 0.0;
		double fCCDenominatorSumSecond = 0.0;

		for (int j = 0; j < n_num_steps; j++) {
			double fCCNumElemFirst = vec_first[j];
			double fCCNumElemSecond = vec_second[j];

			// printf( "%d %f %f\n", j, fCCNumElemFirst, fCCNumElemSecond );

			fCCNumeratorSum += fCCNumElemFirst * fCCNumElemSecond;
			fCCDenominatorSumFirst += fCCNumElemFirst * fCCNumElemFirst;
			fCCDenominatorSumSecond += fCCNumElemSecond * fCCNumElemSecond;
		}
		double fCCDenominator = FastMath.sqrtQuick(fCCDenominatorSumFirst
				* fCCDenominatorSumSecond);
		double fCrossCorrelation = fCCNumeratorSum / fCCDenominator;

		// printf( "Cross Correlation: %f\n", fCrossCorrelation );
		return fCrossCorrelation;
	}

	// metric - root of sum of squares of differences
	public static Double calculateSquaredDifferences(double[] vec1, double[] vec2) {
		double similarity = 0;
		assert(vec1.length == vec2.length);
		for(int i = 0; i < vec1.length; i++){
			similarity += FastMath.powQuick(vec1[i] - vec2[i], 2);
		}
		similarity = FastMath.sqrtQuick(similarity);
		return similarity;
	}
	
	public static Vector2d perpendicularIntersectionPoint(Vector2d p1, Vector2d p2, Vector2d p3) {

		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();

		final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		final Vector2d closestPoint;
		if (u < 0) {
		    closestPoint = p1;
		} else if (u > 1) {
		    closestPoint = p2;
		} else {
		    closestPoint = new Vector2d(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}

		return calculateIntersectionPoint(p1,p2,closestPoint, p3);
	}

	//normalize in the interval [0, 1].
	public static void normalizeAndInvertValues(double[] values, double maxValue,
			double minValue) {
		for(int i = 0; i < values.length; i++){
			values[i] = 1 - (values[i] - minValue)/(maxValue - minValue);
		}
	}

	public static double calculateCosineSimilarity(double[] vec1, double[] vec2){
		double similarity = 0;
		for(int i = 0; i < vec1.length; i++){
			similarity += vec1[i] * vec2[i];
		}
		similarity = similarity / (vec1.length * vec2.length);
		return similarity;
	}


	/**
	 * a generic method that returns the list of keys sorted by value.
	 * @param
	 * @return
	 */
	public static java.util.List<Object> sortKeysByValue(final Map<Object, Object> m) {
		java.util.List <Object> keys = new ArrayList<Object>();
		keys.addAll(m.keySet());
		Collections.sort(keys, new Comparator<Object>() {
			public int compare(Object key1, Object key2) {
				Object v1 = m.get(key1);
				Object v2 = m.get(key2);
				if (v1 == null) {
					return (v2 == null) ? 0 : 1;
				}
				else if (v1 instanceof Comparable) {
					return ((Comparable<Object>) v1).compareTo(v2);
				}
				else {
					return 0;
				}
			}
		});
		return keys;
	}

	public static double distanceBetween(Vector2d p1, Vector2d p2, Vector2d p3){
		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();

		final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		final Vector2d closestPoint;
		if (u < 0) {
			closestPoint = p1;
		} else if (u > 1) {
			closestPoint = p2;
		} else {
			closestPoint = new Vector2d(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}

		return closestPoint.distanceTo(p3);
	}
	
	public static Vector2d intersectLines(Vector2d p1, Vector2d p2, Vector2d p3, Vector2d p4) {
		double x1 = p1.x, x2 = p2.x, x3 = p3.x, x4 = p4.x;
		double y1 = p1.y, y2 = p2.y, y3 = p3.y, y4 = p4.y;
		double d = (y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
		
		if(d==0)// lines are parallel
			return null;
		
		double ua = ((x4-x3)*(y1-y3)-(y4-y3)*(x1-x3)) / d;
		double ub = ((x2-x1)*(y1-y3)-(y2-y1)*(x1-x3)) / d;
		
		if(ua >= 0 && ua <= 1 && ub >=0 && ub <=1)//point inside both lines
			return new Vector2d((x1 + ua*(x2 - x1)),(y1 + ua*(y2 - y1)));
		return null;
	}

	//calculates the intersection between segments (p1, p2) and (p3,p4);
	public static Vector2d calculateIntersectionPoint(Vector2d p1,
			Vector2d p2, Vector2d p3, Vector2d p4) {
		double x1 = p1.x, x2 = p2.x, x3 = p3.x, x4 = p4.x;
		double y1 = p1.y, y2 = p2.y, y3 = p3.y, y4 = p4.y;
		double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
		if (d == 0) 
			return null;
		double xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
		double yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
		return new Vector2d(xi,yi);
	}

	/*******************************************************************************
	 * This file contains datatype definitions and some macros for doing vector
	 * math. All of them are quite simple, but it makes the rest of simulator
	 * code easier to read and understand, as well as lowering the rate of
	 * (nasty) math bugs. At least that is the idea.
	 *******************************************************************************/

	// #define EPSILON 0.0000001
	//
	//
	//
	//
	// struct dRangeBound
	// {
	// char name;
	// double angle;
	// unsigned bound;
	// };

	//
	// /******************************************************************************/
	// // Vector2 macro functions:
	// /******************************************************************************/
	//
	// // Returns the squared length of a vector
	// public static double dVec2LengthSquared(Vector2d vec) {
	// return (vec.x*vec.x + vec.y*vec.y);
	// }
	//
	// // Returns the length of a vector
	// public static double dVec2Length(Vector2d vec){
	// return Math.sqrt(dVec2LengthSquared(vec));
	// }
	//
	// Returns the vector going _from_ A to B (thus B - A)
	// public static void sub(Vector2d result, Vector2d A, Vector2d B)
	// {
	// result.x = B.x - A.x;
	// result.y = B.y - A.y;
	// }
	//
	// // // Adds two vectors:
	// public static void add(Vector2d result, Vector2d A, Vector2d B) {
	// result.x = B.x + A.x;
	// result.y = B.y + A.y;
	// }
	//
	// // Returns the dot product of two vectors:
	// public static double dVec2Dot(Vector2d A, Vector2d B) {
	// return ((A.x * B.x) + (A.y * B.y));
	// }
	//
	// // Projection of A onto B,
	// // where B should be a unit vector (otherwise the result points in the
	// right direction, but the length is multiplied by length(B)):
	// public static void dVec2Projection(Vector2d result, Vector2d A, Vector2d
	// B)
	// {
	// result.x = dVec2Dot(A,B)*B.x;
	// result.y = dVec2Dot(A,B)*B.y;
	// }
	//
	// // Set a vector to (0,0):
	// public static void dVec2Zero(Vector2d A) {
	// A.x = 0;
	// A.y = 0;
	// }
	//
	// // Get the perpendicular dot product
	// // (used when computing the torque of a ridget body):
	// public static double dVec2PrepDot(Vector2d R, Vector2d F){
	// return (-R.y * F.x + R.x * F.y);
	// }
	//
	// // Rotate a vector:
	// public static void rotate(double angle, Vector2d vec) {
	// double xt_ = vec.x;
	// vec.x = Math.cos(angle) * vec.x - Math.sin(angle) * vec.y;
	// vec.y = Math.cos(angle) * vec.y + Math.sin(angle) * xt_;
	// }

	//
	// // Normal of a vector (not to confuse with normalized vector!!!):
	// // result is the right hand normal of vec
	// public static void dVec2Normal(Vector2d result,Vector2d vec)
	// {
	// result.x = -vec.y;
	// result.y = vec.x;
	// }

	// // Normalize a vector:
	// public static void dVec2Normalize(Vector2d vec)
	// {
	// double length___ = dVec2Length(vec);
	// vec.x /= length___;
	// vec.y /= length___;
	// }
	//
	//
	// // // Make vec perpendicular to itself
	// public static void dVec2Perpendicular(Vector2d vec)
	// {
	// double tempx___ = vec.x;
	// vec.x = -vec.y;
	// vec.y = tempx___;
	// }

	// // Multiply a vector by a scalar
	// public static void dVec2MultiplyScalar(Vector2d vec, double scalar)
	// {
	// vec.x *= scalar;
	// vec.y *= scalar;
	// }
	//
	// // // Find the cos to the angle between two vectors
	// public static double dVec2CosAngle(Vector2d vec1, Vector2d vec2){
	// return ((vec1.x * vec2.x + vec1.y * vec2.y) / (dVec2Length(vec1) *
	// dVec2Length(vec2)));
	// }
	//
	// // Find the angle between two vectors
	// public static double dVec2Angle(Vector2d vec1,Vector2d vec2) {
	// return (Math.acos(dVec2CosAngle(vec1, vec2)));
	// }

	// // Find the angle of one vector
	// public static double dVec2OwnAngle(Vector2d vec) {
	// return Math.atan2(vec.y, vec.x);
	// }

	// // Returns the distance between two points
	// public static double dVec2Distance(Vector2d A, Vector2d B){
	// return Math.sqrt(dVec2DistanceSquared(A,B));
	// }
	//
	//
	// // Returns the square distance between two points
	// public static double dVec2DistanceSquared(Vector2d A, Vector2d B){
	// return ( (A.x-B.x)*(A.x-B.x) + (A.y-B.y)*(A.y-B.y) );
	// }

}
