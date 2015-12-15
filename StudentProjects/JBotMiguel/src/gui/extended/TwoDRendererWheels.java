package gui.extended;

import fourwheeledrobot.AWS_3Actuator;
import fourwheeledrobot.FWS_3Actuator;
import fourwheeledrobot.MultipleWheelAxesActuator;
import gui.renderer.TwoDRendererDebug;

import java.awt.Color;
import java.awt.Graphics;

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
		
		Actuator act = robot.getActuatorWithId(1);
		
		if(act instanceof MultipleWheelAxesActuator) {
			MultipleWheelAxesActuator mwaa = (MultipleWheelAxesActuator)act;
			
			int speeds = mwaa.getNumberOfSpeeds();
			int rotations = mwaa.getNumberOfRotations();
			
			int spacing = 30;
			
			if(speeds == 1 && rotations == 1)
				draw1Speed1Rotation(mwaa, robot, spacing);
			else if(speeds == 1 && rotations == 2 && mwaa instanceof FWS_3Actuator)
				draw1Speed2RotationsFront(mwaa, robot, spacing);
			else if(speeds == 1 && rotations == 2 && mwaa instanceof AWS_3Actuator)
				draw1Speed2RotationsBoth(mwaa, robot, spacing);
			else if(speeds == 2 && rotations == 2)
				draw2Speed2Rotations(mwaa, robot, spacing);
			else if(speeds == 4 && rotations == 4)
				draw4Speed4Rotations(mwaa, robot, spacing);
			else if(speeds == 2 && rotations == 4)
				draw2Speed4Rotations(mwaa, robot, spacing);
			
		}
	}
	
	public void drawTwoWheelsZeroAxes(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		
	}
	
	public void draw1Speed1Rotation(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		
		double rO = -r.getOrientation();
		double speed = mwaa.getSpeed()[0];
		
		double[] speeds = new double[]{speed,speed,speed,speed};
		double[] rotations = mwaa.getRotation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		double rotAngle = rO;
		
		for(int i = 0 ; i < 4 ; i++) {
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			if(column == -1) {
				rotAngle = rO;
			} else {
				rotAngle = rO-rotations[0];
			}
			
			int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
			drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
		}
		drawRobotOverlay(robotCenter, rO, spacing);		
	}
	
	//CAR2
	public void draw1Speed2RotationsFront(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		
		double rO = -r.getOrientation();
		double speed = mwaa.getSpeed()[0];
		
		double[] speeds = new double[]{speed,speed,speed,speed};
		double[] rotations = mwaa.getRotation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		double rotAngle = rO;
		
		for(int i = 0 ; i < 4 ; i++) {
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			if(column == -1) {
				rotAngle = rO;
			} else {
				if(row == -1) {
					rotAngle = rO-rotations[0];
				} else {
					rotAngle = rO-rotations[1];
				}
			}
			
			int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
			drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
		}
		drawRobotOverlay(robotCenter, rO, spacing);		
	}
	
	//CAR2AS
	public void draw1Speed2RotationsBoth(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		
		double rO = -r.getOrientation();
		double speed = mwaa.getSpeed()[0];
		
		double[] speeds = new double[]{speed,speed,speed,speed};
		double[] rotations = mwaa.getRotation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		double rotAngle = rO;
		
		for(int i = 0 ; i < 4 ; i++) {
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			if(column == -1) {
				rotAngle = rO-rotations[1];
			} else {
				rotAngle = rO-rotations[0];
			}
			
			int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
			drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
		}
		drawRobotOverlay(robotCenter, rO, spacing);		
	}
	
	//CAR2DAS
	public void draw2Speed2Rotations(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		
		double rO = -r.getOrientation();
		double[] speeds = new double[]{mwaa.getSpeed()[0],mwaa.getSpeed()[0],mwaa.getSpeed()[1],mwaa.getSpeed()[1]};
		double[] rotations = mwaa.getRotation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		double rotAngle = rO;
		
		for(int i = 0 ; i < 4 ; i++) {
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			if(column == -1) {
				rotAngle = rO-rotations[1];
			} else {
				rotAngle = rO-rotations[0];
			}
			
			int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
			drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
		}
		drawRobotOverlay(robotCenter, rO, spacing);		
	}
	
	public void drawFourWheelsTwoAxes(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		double rO = -r.getOrientation();
		
		double[] speeds = mwaa.getSpeed();
		double[] rotations = mwaa.getRotation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		for(int i = 0 ; i < speeds.length ; i++) {
			
			int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
			double rotAngle = -rotations[i/2] + rO;
			
			int row = getWheelRow(i);
			int column = getWheelColumn(i);
			
			drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
		}
		
		drawRobotOverlay(robotCenter, rO, spacing);
	}
	
	//AWS_6
		public void draw2Speed4Rotations(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
			
			double rO = -r.getOrientation();
			
			double[] speeds = new double[]{mwaa.getSpeed()[0],mwaa.getSpeed()[0],mwaa.getSpeed()[1],mwaa.getSpeed()[1]};
			double[] rotations = mwaa.getRotation();
			
			Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
			
			for(int i = 0 ; i < speeds.length ; i++) {
				
				int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
				
				double rotAngle = -rotations[i] + rO;
				
				int row = getWheelRow(i);
				int column = getWheelColumn(i);
				
				drawRobotWheels(column, row, spacing, rO, robotCenter, rotAngle, len);
			}
			
			drawRobotOverlay(robotCenter, rO, spacing);
		}
	
	//AWS_8
	public void draw4Speed4Rotations(MultipleWheelAxesActuator mwaa, Robot r, int spacing) {
		
		double rO = -r.getOrientation();
		
		double[] speeds = mwaa.getSpeed();
		double[] rotations = mwaa.getRotation();
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		for(int i = 0 ; i < speeds.length ; i++) {
			
			int len = (int)(spacing*speeds[i]/mwaa.getMaxSpeed());
			
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
		
		graphics.setColor(Color.RED);
		graphics.drawOval((int)posA.x-wheelSize,(int)posA.y-wheelSize,wheelSize*2,wheelSize*2);
		graphics.drawLine((int)posA.x, (int)posA.y, (int)posB.x, (int)posB.y);
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