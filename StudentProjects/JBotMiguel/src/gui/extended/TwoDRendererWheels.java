package gui.extended;

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
			
			int wheels = mwaa.getNumberOfWheels();
			int axes = mwaa.getNumberOfAxes();
			
			if(wheels == 2 && axes == 0)
				drawTwoWheelsZeroAxes(mwaa, robot);
			else if(wheels == 4 && axes == 0)
				drawFourWheelsZeroAxes(mwaa, robot);
			else if(wheels == 4 && axes == 2)
				drawFourWheelsTwoAxes(mwaa, robot);
			else if(wheels == 4 && axes == 4)
				drawFourWheelsFourAxes(mwaa, robot);
			
		}
	}
	
	public void drawTwoWheelsZeroAxes(MultipleWheelAxesActuator mwaa, Robot r) {
		
	}
	
	public void drawFourWheelsZeroAxes(MultipleWheelAxesActuator mwaa, Robot r) {
		
	}
	
	public void drawFourWheelsTwoAxes(MultipleWheelAxesActuator mwaa, Robot r) {
		
	}
	
	public void drawFourWheelsFourAxes(MultipleWheelAxesActuator mwaa, Robot r) {
		
		double rO = -r.getOrientation();
		double rOc = Math.cos(rO);
		double rOs = Math.sin(rO);
		
		double[] speeds = mwaa.getSpeed();
		double[] rotations = mwaa.getRotation();
		for(int i = 0 ; i < speeds.length ; i++) {
//			speeds[i] = 0.1;
//			rotations[i] = 0;
		}
		
		int spacing = 30;
		int maxLen = spacing;
		int wheelSize = spacing/5;
		
		Vector2d robotCenter = new Vector2d(spacing*2.5,spacing*2.5);
		
		for(int i = 0 ; i < speeds.length ; i++) {
			
			int len = (int)(maxLen*speeds[i]/mwaa.getMaxSpeed());
			
			double rotAngle = -rotations[i]-Math.PI/2 + rO;
			
			int row = i < 2 ? -1 : 1;
			int column = (i % 2)*2 - 1;
			
			Vector2d posA = new Vector2d(column*spacing,row*spacing);
			posA.rotate(rO);
			posA.add(robotCenter);
			
			Vector2d posB = new Vector2d(posA);
//			Vector2d posB = new Vector2d(column,row);
//			posA.add(new Vector2d()rO);
//			System.out.println(posA);
			
			Vector2d rot = new Vector2d(len*Math.cos(rotAngle),len*Math.sin(rotAngle));
			posB.add(rot);
			
//			posA.add(robotCenter);
//			posB.add(robotCenter);
			
//			posA.add(new Vector2d(spacing*2+spacing*column,spacing*2+spacing*row));
//			posB.add(new Vector2d(spacing*2+spacing*column,spacing*2+spacing*row));
		
			
			graphics.setColor(Color.RED);
			graphics.drawOval((int)posA.x-wheelSize,(int)posA.y-wheelSize,wheelSize*2,wheelSize*2);
			graphics.drawLine((int)posA.x, (int)posA.y, (int)posB.x, (int)posB.y);
		}
		
		int ovalSize = (int)(spacing*2.5);
		graphics.setColor(Color.BLACK);
		graphics.drawOval((int)robotCenter.x-ovalSize/2, (int)robotCenter.y-ovalSize/2, (int)ovalSize, (int)ovalSize);
		
		Vector2d robotFront = new Vector2d(rOc*ovalSize/2.5,rOs*ovalSize/2.5);
		robotFront.add(robotCenter);
		Vector2d robotCenterLeft = new Vector2d(robotCenter);
		robotCenterLeft.add(new Vector2d(10*Math.cos(rO+Math.PI/2),10*Math.cos(rO+Math.PI/2)));
		
		graphics.drawLine((int)robotCenterLeft.x, (int)robotCenterLeft.y, (int)robotFront.x, (int)robotFront.y);
		graphics.drawLine((int)robotCenter.x, (int)robotCenter.y, (int)robotFront.x, (int)robotFront.y);
		
//		graphics.drawRect(spacing, spacing, spacing, spacing);
		
	}
	
	

}
