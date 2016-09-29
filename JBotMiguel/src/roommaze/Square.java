package roommaze;

import java.io.Serializable;

public class Square implements Serializable{
	double x;
	double y;
	int walls[] = {0,0,0,0};
	double distanceToFinish = 0;
	
	public double getDistance() {
		return distanceToFinish;
	}
	
	public int[] getWalls() {
		return walls;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return x+" "+y+" "+distanceToFinish;
	}
}