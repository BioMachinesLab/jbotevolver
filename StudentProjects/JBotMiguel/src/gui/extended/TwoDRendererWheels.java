package gui.extended;

import fourwheeledrobot.MultipleWheelAxesActuator;
import fourwheeledrobot.MultipleWheelRepertoireActuator;
import fourwheeledrobot.MultipleWheelRepertoireNDActuator;
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
	
	protected void drawRobotBasic(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();
		int circleDiameter = bigRobots ? (int)Math.max(10,Math.round(robot.getDiameter() * scale)) : (int) Math.round(robot.getDiameter() * scale);
		int x = transformX(robot.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(robot.getPosition().getY()) - circleDiameter / 2;
		
		Graphics2D g2d = (Graphics2D)graphics;
		g2d.setStroke(new BasicStroke(2));
		
		graphics.setColor(Color.RED);
		g2d.drawOval(x, y, circleDiameter, circleDiameter);
		
		
		double orientation  = robot.getOrientation();
		
		double x2 = robot.getPosition().getX()+robot.getDiameter()/2.0*Math.cos(orientation);
		double y2 = robot.getPosition().getY()+robot.getDiameter()/2.0*Math.sin(orientation);
		
		graphics.drawLine(transformX(robot.getPosition().getX()), transformY(robot.getPosition().getY()), transformX(x2), transformY(y2));

		g2d.setStroke(new BasicStroke(1));
		
		graphics.setColor(Color.BLACK);
	}
	
	@Override
	protected void drawRobot(Graphics graphics, Robot robot) {
//		super.drawRobot(graphics, robot);
		
		drawRobotBasic(graphics,robot);
		
		if(robot.getActuators().size() >= 1) {
			
			Actuator act = robot.getActuatorWithId(1);
	
			int spacing = 30;
			
			if(act instanceof MultipleWheelAxesActuator) {
				MultipleWheelAxesActuator mwaa = (MultipleWheelAxesActuator)act;
				drawSpeedRotation(mwaa.getCompleteSpeeds(),mwaa.getCompleteRotations(), robot, spacing, mwaa.getMaxSpeed());
			} else if(act instanceof MultipleWheelRepertoireActuator) {
				MultipleWheelRepertoireActuator mwaa = (MultipleWheelRepertoireActuator)act;
				drawSpeedRotation(mwaa.getCompleteSpeeds(),mwaa.getCompleteRotations(), robot, spacing, mwaa.getMaxSpeed());
			} else if(act instanceof MultipleWheelRepertoireNDActuator) {
				MultipleWheelRepertoireNDActuator mwaa = (MultipleWheelRepertoireNDActuator)act;
				drawSpeedRotation(mwaa.getCompleteSpeeds(),mwaa.getCompleteRotations(), robot, spacing, mwaa.getMaxSpeed());
			}
		}
	}
	
	public void drawSpeedRotation(double[] speeds, double[] rotations, Robot r, int spacing, double maxSpeed) {
		
		double robotOrientation = -r.getOrientation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		for(int i = 0 ; i < speeds.length ; i++) {
			
			int len = (int)(spacing*speeds[i]/maxSpeed);
			
			double wheelAngle = -rotations[i] + robotOrientation;
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			drawRobotWheels(column, row, spacing, robotOrientation, robotCenter, wheelAngle, len);
		}
		
		drawRobotOverlay(robotCenter, robotOrientation, spacing);
	}
	
	protected int getWheelRow(int i) {
		if(i > 0 && i < 3) return 1;
		return -1;
	}
	
	protected int getWheelColumn(int i) {
		if (i < 2) return 1;
		return -1;
	}
	
	protected void drawRobotWheels(int column, int row, int spacing, double robotOrientation, Vector2d robotCenter, double wheelAngle, int len) {
		
		int wheelSize = spacing/5;
		Vector2d wheelCenter = new Vector2d(column*spacing,row*spacing);
		wheelCenter.rotate(robotOrientation);
		wheelCenter.add(robotCenter);
		
		Vector2d posB = new Vector2d(wheelCenter);
		
		Vector2d rot = new Vector2d(len*Math.cos(wheelAngle),len*Math.sin(wheelAngle));
		posB.add(rot);
		
		Vector2d guidingLineCenter = new Vector2d(wheelCenter);
		rot = new Vector2d(len*Math.cos(robotOrientation),len*Math.sin(robotOrientation));
		guidingLineCenter.add(rot);
		
		Vector2d guidingLineLeft = new Vector2d(wheelCenter);
		rot = new Vector2d(len*Math.cos(robotOrientation+Math.PI/4),len*Math.sin(robotOrientation+Math.PI/4));
		guidingLineLeft.add(rot);
		
		Vector2d guidingLineRight = new Vector2d(wheelCenter);
		rot = new Vector2d(len*Math.cos(robotOrientation-Math.PI/4),len*Math.sin(robotOrientation-Math.PI/4));
		guidingLineRight.add(rot);
		
		Graphics2D gx = (Graphics2D)graphics;
		
		//guiding lines
		gx.setColor(Color.LIGHT_GRAY);
		gx.drawLine((int)wheelCenter.x, (int)wheelCenter.y, (int)guidingLineLeft.x, (int)guidingLineLeft.y);
		gx.drawLine((int)wheelCenter.x, (int)wheelCenter.y, (int)guidingLineRight.x, (int)guidingLineRight.y);
		gx.drawLine((int)wheelCenter.x, (int)wheelCenter.y, (int)guidingLineCenter.x, (int)guidingLineCenter.y);
		
		
		//actual line
		gx.setStroke(new BasicStroke(2));
		gx.setColor(Color.RED);
		gx.drawOval((int)wheelCenter.x-wheelSize,(int)wheelCenter.y-wheelSize,wheelSize*2,wheelSize*2);
		gx.drawLine((int)wheelCenter.x, (int)wheelCenter.y, (int)posB.x, (int)posB.y);
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
		
		graphics.drawPolygon(
				new int[]{(int)robotCenterLeft.x, (int)robotCenterRight.x, (int)robotFront.x},
				new int[]{(int)robotCenterLeft.y, (int)robotCenterRight.y, (int)robotFront.y},
				3);
	}
	
	
}