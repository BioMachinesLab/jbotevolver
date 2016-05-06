package gui.extended;

import fourwheeledrobot.MultipleWheelAxesActuator;
import fourwheeledrobot.MultipleWheelRepertoireActuator;
import gui.renderer.TwoDRendererDebug;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import mathutils.Vector2d;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class TwoDRendererWheels extends TwoDRendererDebug{

	public TwoDRendererWheels(Arguments args) {
		super(args);
	}
	
	@Override
	protected void drawRobot(Graphics graphics, Robot robot) {
		super.drawRobot(graphics, robot);
		
		if(robot.getActuators().size() > 1) {
			
			Actuator act = robot.getActuatorWithId(1);
	
			int spacing = 30;
			
	//		if(act instanceof MultipleWheelAxesActuator) {
	//			MultipleWheelAxesActuator mwaa = (MultipleWheelAxesActuator)act;
	//			drawSpeedRotation(mwaa.getCompleteSpeeds(),mwaa.getCompleteRotations(), robot, spacing, mwaa.getMaxSpeed());
	//		} else if(act instanceof MultipleWheelRepertoireActuator) {
	//			MultipleWheelRepertoireActuator mwaa = (MultipleWheelRepertoireActuator)act;
	//			drawSpeedRotation(mwaa.getCompleteSpeeds(),mwaa.getCompleteRotations(), robot, spacing, mwaa.getMaxSpeed());
	//		}
		}
	}
	
	public void drawSpeedRotation(double[] speeds, double[] rotations, Robot r, int spacing, double maxSpeed) {
		
		double rO = -r.getOrientation();
		
//		for(double s : speeds)
//			System.out.print(s+" ");
//		System.out.println();
//		for(double s : rotations)
//			System.out.print(s+" ");
//		System.out.println();
//		System.out.println();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		for(int i = 0 ; i < speeds.length ; i++) {
			
			int len = (int)(spacing*speeds[i]/maxSpeed);
			
			double rotAngle = -rotations[i] + rO;
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
		}
		
		drawRobotOverlay(robotCenter, rO, spacing);
	}
	
	protected int getWheelRow(int i) {
		if(i > 0 && i < 3) return 1;
		return -1;
	}
	
	protected int getWheelColumn(int i) {
		if (i < 2) return 1;
		return -1;
	}
	
	protected void drawRobotWheels(int column, int row, int spacing, double rO, Vector2d robotCenter, double rotAngle, int len) {
		
		int wheelSize = spacing/5;
		Vector2d posA = new Vector2d(column*spacing,row*spacing);
		posA.rotate(rO);
		posA.add(robotCenter);
		
		Vector2d posB = new Vector2d(posA);
		
		Vector2d rot = new Vector2d(len*Math.cos(rotAngle),len*Math.sin(rotAngle));
		posB.add(rot);
		
		Vector2d posC = new Vector2d(posA);
		rot = new Vector2d(len*Math.cos(rO),len*Math.sin(rO));
		posC.add(rot);
		
		Graphics2D gx = (Graphics2D)graphics;
		
		//guiding line
		gx.setColor(Color.BLACK);
		gx.drawLine((int)posA.x, (int)posA.y, (int)posC.x, (int)posC.y);
		
		gx.setStroke(new BasicStroke(2));
		gx.setColor(Color.RED);
		gx.drawOval((int)posA.x-wheelSize,(int)posA.y-wheelSize,wheelSize*2,wheelSize*2);
		gx.drawLine((int)posA.x, (int)posA.y, (int)posB.x, (int)posB.y);
		gx.setStroke(new BasicStroke());
	}
	
	protected void drawRobotOverlay(Vector2d robotCenter, double rO, int spacing) {
		int ovalSize = (int)(spacing*2.5);
		graphics.setColor(Color.BLACK);
		graphics.drawOval((int)robotCenter.x-ovalSize/2, (int)robotCenter.y-ovalSize/2, (int)ovalSize, (int)ovalSize);
		
		Vector2d robotFront = new Vector2d(Math.cos(rO)*ovalSize/2.5,Math.sin(rO)*ovalSize/2.5);
		robotFront.add(robotCenter);
		Vector2d robotCenterLeft = new Vector2d(robotCenter);
		robotCenterLeft.add(new Vector2d(10*Math.cos(rO+Math.PI/2),10*Math.sin(rO+Math.PI/2)));
		
		Vector2d robotCenterRight = new Vector2d(robotCenter);
		robotCenterRight.add(new Vector2d(10*Math.cos(rO-Math.PI/2),10*Math.sin(rO-Math.PI/2)));
		
		graphics.fillPolygon(
				new int[]{(int)robotCenterLeft.x, (int)robotCenterRight.x, (int)robotFront.x},
				new int[]{(int)robotCenterLeft.y, (int)robotCenterRight.y, (int)robotFront.y},
				3);
	}
}