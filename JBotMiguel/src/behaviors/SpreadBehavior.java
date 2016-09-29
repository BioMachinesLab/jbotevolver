package behaviors;

import java.awt.Color;
import java.util.LinkedList;

import behaviors.Behavior;
import mathutils.MathUtils;
import mathutils.Vector2d;
import sensors.RobotPositionSensor;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SpreadBehavior extends Behavior {
	
	private RobotPositionSensor rps;
	private DifferentialDriveRobot r;
	private int lineDistanceThreshold = 50;
	private int robotDistanceThreshold = 100;

	public SpreadBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		rps = (RobotPositionSensor) r.getSensorByType(RobotPositionSensor.class);
		this.r = (DifferentialDriveRobot)r;
	}
	
	@Override
	public void controlStep(double time) {
		
		double centerX = 0;
		double centerY = 0;
		
		r.setWheelSpeed(0, 0);
		
		LinkedList<PhysicalObject> pos = rps.getPositions();
		
		if(!pos.isEmpty()) {
			
			int division = 0;
			
			for(PhysicalObject p : pos) {
				Vector2d v = p.getPosition();
				int weight = 1;
				
				double objectX = v.getX();
				double objectY = v.getY();
				
				if(p.getType() == PhysicalObjectType.LINE) {
					Line l = (Line)p;
					Vector2d closestPoint = MathUtils.perpendicularIntersectionPoint(l.getPointA(),l.getPointB(), robot.getPosition());
					
					objectX = closestPoint.getX();
					objectY = closestPoint.getY();
					
					double distance = closestPoint.distanceTo(robot.getPosition());
					if(distance < lineDistanceThreshold) {
						weight = lineDistanceThreshold-(int)distance;
					} else {
						weight = 0;
					}
					
				} else {
					double distance = v.distanceTo(robot.getPosition());
					weight = (int)(robotDistanceThreshold/distance);
				}
				
				
				for(int i = 0 ; i < weight ; i++) {
					centerX+=objectX;
					centerY+=objectY;
				}
				division+=weight;
			}
			
			if(division > 0) {
				centerX/=(double)division;
				centerY/=(double)division;
				
				Vector2d p = new Vector2d(centerX,centerY);
				
				GeometricCalculator g = new GeometricCalculator();
				GeometricInfo i = g.getGeometricInfoBetweenPoints(robot.getPosition(), robot.getOrientation(), p, time);
				
				double a = i.getAngle();
				
				if(i.getDistance() > 1) {
				
					if(a > 0)
						a-=Math.PI;
					else
						a+=Math.PI;
					
					if(Math.abs(a) > 0.17453*2) { //20 deg
						double speed = a < 0 ? -1 : 1;
						r.setWheelSpeed(speed, -speed);
					} else {
						r.setWheelSpeed(2.7, 2.7);
					}
				}
			}
		}
	}
}