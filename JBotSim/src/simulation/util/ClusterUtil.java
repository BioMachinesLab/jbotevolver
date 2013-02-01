package simulation.util;

import java.util.ArrayList;

import mathutils.Vector2d;

import simulation.robot.Robot;

public class ClusterUtil {
	
	private ArrayList<Robot> robots;
	private Robot robot;
	private Vector2d coord;
	public ClusterUtil(Robot robot){
		this.robot=robot;
		robots=new ArrayList<Robot>();
		robots.add(robot);
		this.coord=getCenterMass();
	}
	
	public Vector2d centerMass(){
		return coord;
	}
	
	private Vector2d getCenterMass(){
		Vector2d coord = new Vector2d();
		double x=0;
		double y=0;
		for(Robot r: robots){
			x+=r.getPosition().x;
			y+=r.getPosition().y;
		}
		x=x/robots.size();
		y=y/robots.size();
		coord.set(x,y);
		
		return coord;
		
	}
	
	public double getDistance(ClusterUtil cluster){
		return this.coord.distanceTo(cluster.coord);
	}
	public ArrayList<Robot> getClusterElements(){
		return robots;
	}
	
	public void addRobots(ArrayList<Robot> otherRobots){
		robots.addAll(otherRobots);
		this.coord=getCenterMass();
	}
	
	public void removeAllRobots(){
		robots.clear();
	}
	

}
