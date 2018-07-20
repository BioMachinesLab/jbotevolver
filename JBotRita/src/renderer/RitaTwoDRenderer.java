package renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import net.jafama.FastMath;
import mathutils.Vector2d;
import robots.JumpingRobot;
import robots.JumpingSumo;
import sensors.PreyTakingAccountWallsSensor;
import sensors.RobotsJumpingConeSensor;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Wall.Edge;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.PreySensor;
import simulation.robot.sensors.RobotSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;
import gui.renderer.TwoDRenderer;

public class RitaTwoDRenderer extends TwoDRenderer {
	private Vector2d oldPosition;
	protected int selectedRobot = -1;
	private boolean wallRay;
	private int coneSensorId;
	private String coneClass = "";
	private int robotId;
	private boolean boardSensors;
	private boolean paperSensors;

	public RitaTwoDRenderer(Arguments args) {
		super(args);
		wallRay = args.getArgumentAsIntOrSetDefault("wallray", 0) == 1;
		coneSensorId = args.getArgumentAsIntOrSetDefault("conesensorid", -1);
		coneClass = args.getArgumentAsStringOrSetDefault("coneclass", "");
		robotId = args.getArgumentAsIntOrSetDefault("robotid", -1);
		boardSensors = args.getArgumentAsIntOrSetDefault("boardsensors", 0) == 1;
		paperSensors = args.getArgumentAsIntOrSetDefault("papersensors", 0) == 1;
	}

	@Override
	protected void drawRobot(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();

		int circleDiameter = bigRobots ? (int) Math.max(10, Math.round(robot.getDiameter() * scale)) : (int) Math .round(robot.getDiameter() * scale);
		//int circleDiameter = (int) Math.round(robot.getDiameter() * 45);
		int x = (int) (transformX(robot.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(robot.getPosition().getY()) - circleDiameter / 2);

		// if(robot.getId() == selectedRobot) {
		// graphics.setColor(Color.yellow);
		// graphics.fillOval(x-2, y-2, circleDiameter + 4, circleDiameter + 4);
		//
		// }

		// se saltar, passar cor para vermelho

		// if(robot instanceof JumpingSumo && ((JumpingSumo)
		// robot).isJumping()){
		// if (((JumpingSumo)robot).isJumpingUp()) {
		// robot.setBodyColor(Color.GREEN);
		// } else {
		// robot.setBodyColor(Color.ORANGE);
		// }
		//
		// } else if (robot.isInvolvedInCollisonWall()) {
		// robot.setBodyColor(Color.PINK);
		// } else
		// robot.setBodyColor(Color.BLACK);
		//

		
//		aqui
//		if (robot instanceof JumpingSumo) {
//			if (((JumpingSumo) robot).statusOfJumping())
//				robot.setBodyColor(Color.GREEN);
//			else
//				robot.setBodyColor(Color.YELLOW);
//		}
//		graphics.setColor(robot.getBodyColor());

		
		
		if (robot instanceof JumpingSumo) {
			if (((JumpingSumo) robot).isJumping())
				robot.setBodyColor(Color.GREEN);
			else if (robot.isInvolvedInCollisonWall()) {
				robot.setBodyColor(Color.PINK);
			} else if (((JumpingSumo) robot).isDrivingAfterJumping())
				robot.setBodyColor(Color.ORANGE);
			else
				robot.setBodyColor(Color.BLACK);
		}
//
//		} else if (robot.isInvolvedInCollisonWall()) {
//			robot.setBodyColor(Color.PINK);
//		} else
//			robot.setBodyColor(Color.BLACK);
		
		robot.setBodyColor(robot.getBodyColor());
		
		graphics.setColor(robot.getBodyColor());
		graphics.fillOval(x, y, circleDiameter, circleDiameter);

		int avgColor = (robot.getBodyColor().getRed()
				+ robot.getBodyColor().getGreen() + robot.getBodyColor()
				.getBlue()) / 3;

		if (avgColor > 255 / 2) {
			graphics.setColor(Color.BLACK);
		} else {
			graphics.setColor(Color.WHITE);
		}

		double orientation = robot.getOrientation();
		Vector2d p0 = new Vector2d();
		Vector2d p1 = new Vector2d();
		Vector2d p2 = new Vector2d();
		p0.set(0, -robot.getRadius() / 3);
		p1.set(0, robot.getRadius() / 3);
		p2.set(6 * robot.getRadius() / 7, 0);

		p0.rotate(orientation);
		p1.rotate(orientation);
		p2.rotate(orientation);

		int[] xp = new int[3];
		int[] yp = new int[3];

		xp[0] = transformX(p0.getX() + robot.getPosition().getX());
		yp[0] = transformY(p0.getY() + robot.getPosition().getY());

		xp[1] = transformX(p1.getX() + robot.getPosition().getX());
		yp[1] = transformY(p1.getY() + robot.getPosition().getY());

		xp[2] = transformX(p2.getX() + robot.getPosition().getX());
		yp[2] = transformY(p2.getY() + robot.getPosition().getY());

		graphics.fillPolygon(xp, yp, 3);

		graphics.setColor(Color.BLACK);
	

	}

	protected void drawPreys(Graphics graphics, Prey prey) {

		int circleDiameter = (int) Math.round(0.5 + prey.getDiameter() * scale);
		int x = (int) (transformX(prey.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(prey.getPosition().getY()) - circleDiameter / 2);

		if (prey.isEnabled()) {
			graphics.setColor(prey.getColor());
		} else {
			graphics.setColor(Color.gray);
		}
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);
		
		
	}

	@Override
	protected void drawCones(Graphics graphics, Robot robot){
		
		if(robot.getId()==0){
		if(robotId != -1 && robot.getId() != robotId)
			return;
		ArrayList<Integer> listOfSensores = new ArrayList<Integer>();
		//listOfSensores.add(1);
		
		for (Integer coneSensorId : listOfSensores) {
			
		
			
			
		if(!robot.getDescription().equals("prey")){
			if(coneSensorId >= 0 || !coneClass.isEmpty()){
			//System.out.println("entrei nos equalsprey");
				for(Sensor s : robot.getSensors()){
					if(s.getClass().getSimpleName().equals(coneClass) || s.getId() == coneSensorId){
						if(s != null && s instanceof ConeTypeSensor){
							ConeTypeSensor preySensor = (ConeTypeSensor)s;
							for (int i = 0; i < preySensor.getAngles().length ; i++) {
								double angle = preySensor.getAngles()[i];
//							for (Double angle : preySensor.getAngles()) {
							
								double xi;
								double yi;
								
								if(boardSensors){
									xi = robot.getPosition().getX()+robot.getRadius()*FastMath.cosQuick(angle + robot.getOrientation());
									yi = robot.getPosition().getY()+robot.getRadius()*FastMath.sinQuick(angle + robot.getOrientation());
									
								}else{
									xi = robot.getPosition().getX();
									yi = robot.getPosition().getY();
								}
								
								double cutOff = preySensor.getCutOff();
								double openingAngle = preySensor.getOpeningAngle();

								int x1 = transformX(xi);
								int y1 = transformY(yi);
								
								int x3 = transformX(xi-cutOff);
								int y3 = transformY(yi+cutOff);
								
								int a1 = (int)(FastMath.round(FastMath.toDegrees(preySensor.getSensorsOrientations()[i] + robot.getOrientation() - openingAngle/2)));
								
								Graphics2D graphics2D = (Graphics2D) graphics.create();

								if(cutOff > 0){
									//System.out.println("entrei nos cutOff");

									Point2D p = new Point2D.Double(x1, y1);
									float radius = (float) (cutOff*scale);
									float[] dist = {0.0f, 1.0f};
//									Color[] colors = {Color.DARK_GRAY, Color.LIGHT_GRAY};
//									RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
//									graphics2D.setPaint(rgp);
									
									
									if (preySensor instanceof RobotSensor) {
//										System.out.println("eentrei no sensor");

									RobotSensor firesensor = (RobotSensor) preySensor;
//									System.out.println("sensor numero"+i);
//									System.out.println("sensor tenho"+firesensor.getIsJumping(i));

//									if (firesensor.getIsJumping(i).equals( "POSITIVE")) {
//										Color[] colors2 = {
//												Color.DARK_GRAY,
//												Color.PINK };
//										RadialGradientPaint rgp = new RadialGradientPaint(
//												p, radius, dist,
//												colors2);
//										graphics2D.setPaint(rgp);
//										// }
//									} 
//									 if(firesensor.getIsJumping(i).equals("NO")) {
//										Color[] colors2 = {
//												Color.GRAY,
//												Color.DARK_GRAY };
//										RadialGradientPaint rgp = new RadialGradientPaint(
//												p, radius, dist,
//												colors2);
//										graphics2D.setPaint(rgp);
//									}	 if(firesensor.getIsJumping(i).equals("NULL")) {
//										Color[] colors2 = {
//												Color.BLUE,
//												Color.DARK_GRAY };
//										RadialGradientPaint rgp = new RadialGradientPaint(
//												p, radius, dist,
//												colors2);
//										graphics2D.setPaint(rgp);
//									}
									

								}
									
								
								}
								
								
								if(paperSensors)
									graphics2D.setColor(Color.LIGHT_GRAY);
								
								graphics2D.fillArc(x3, y3, (int)FastMath.round(cutOff*2*scale), (int)(FastMath.round(cutOff*2*scale)), a1, (int)FastMath.round(FastMath.toDegrees(openingAngle)));
							

//								graphics2D.setColor(Color.BLACK);
//								graphics2D.drawArc(x2, y2, (int)FastMath.round(range*2*scale), (int)(FastMath.round(range*2*scale)), a1, (int)FastMath.round(FastMath.toDegrees(openingAngle)));
//								graphics2D.drawLine(x1, y1, gx1, gy1);
//								graphics2D.drawLine(x1, y1, gx2, gy2);
							}
						}
					}
				}
			}
		}
		}
		}
	}
	
	
//	@Override
//	protected void drawCones(Graphics graphics, Robot robot) {
//		
//		if(robot.getId()==0){
//		
//		if (robotId != -1 && robot.getId() != robotId)
//			return;
//		ArrayList<Integer> listOfSensores = new ArrayList<Integer>();
//		listOfSensores.add(1);
//		//listOfSensores.add(1);
//		//listOfSensores.add(2);
//
//		//listOfSensores.add(3);
//		boolean secondTime = false;
//
//		for (Integer sensorId : listOfSensores) {
//			if (!robot.getDescription().equals("prey")) {
//				if (sensorId >= 0 || !coneClass.isEmpty()) {
//					// if(coneSensorId >= 0 || !coneClass.isEmpty()){
//					for (Sensor s : robot.getSensors()) {
//						if (s.getClass().getSimpleName().equals(coneClass)
//								|| s.getId() == sensorId) {
//							if (s != null && s instanceof ConeTypeSensor) {
//								ConeTypeSensor coneSensor = (ConeTypeSensor) s;
//								for (int i = 0; i < coneSensor.getAngles().length; i++) {
//
//									double angle = coneSensor.getAngles()[i];
//									// for (Double angle :
//									// preySensor.getAngles()) {
//
//									double xi;
//									double yi;
//
//									if (boardSensors) {
//										xi = robot.getPosition().getX()
//												+ robot.getRadius()
//												* FastMath
//														.cosQuick(angle
//																+ robot.getOrientation());
//										yi = robot.getPosition().getY()
//												+ robot.getRadius()
//												* FastMath
//														.sinQuick(angle
//																+ robot.getOrientation());
//
//									} else {
//										xi = robot.getPosition().getX();
//										yi = robot.getPosition().getY();
//									}
//
//									double cutOff = coneSensor.getCutOff();
//									double openingAngle = coneSensor
//											.getOpeningAngle();
//
//									int x1 = transformX(xi);
//									int y1 = transformY(yi);
//
//									int x3 = transformX(xi - cutOff);
//									int y3 = transformY(yi + cutOff);
//
//									int a1 = (int) (FastMath
//											.round(FastMath.toDegrees(coneSensor
//													.getSensorsOrientations()[i]
//													+ robot.getOrientation()
//													- openingAngle / 2)));
//
//									Graphics2D graphics2D = (Graphics2D) graphics
//											.create();
//
//									if (cutOff > 0) {
//										Point2D p = new Point2D.Double(x1, y1);
//										float radius = (float) (cutOff * scale);
//										float[] dist = { 0.0f, 1.0f };
//
////										if (coneSensor instanceof JumpRobotsSensor) {
////											JumpRobotsSensor firesensor = (JumpRobotsSensor) coneSensor;
////											if (firesensor.getIsJumping(i) == 1.0) {
////												Color[] colors2 = {
////														Color.DARK_GRAY,
////														Color.PINK };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////												// }
////											} else if(firesensor.getIsJumping(i) == 0.5) {
////												Color[] colors2 = {
////														Color.YELLOW,
////														Color.GRAY };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////											}
////											else if(firesensor.getIsJumping(i) == 0.0) {
////												Color[] colors2 = {
////														Color.GRAY,
////														Color.DARK_GRAY };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////											}
////											else if(firesensor.getIsJumping(i) == 0.6) {
////												Color[] colors2 = {
////														Color.BLACK,
////														Color.DARK_GRAY };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////											}
////
////										}
////										
//										
////										if (coneSensor instanceof PreySensor) {
////											PreySensor firesensor = (PreySensor) coneSensor;
////											if (firesensor.getIsJumping(i) > 0.0) {
////												System.out.println("vou ser cor-de-rosa");
////												Color[] colors2 = {
////														Color.DARK_GRAY,
////														Color.PINK };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////												// }
////											} 
////											else if(firesensor.getIsJumping(i) == 0.0) {
////												Color[] colors2 = {
////														Color.GRAY,
////														Color.DARK_GRAY };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////											}
////											
////
////										}
//										
//										if (coneSensor instanceof RobotSensor) {
//											RobotSensor firesensor = (RobotSensor) coneSensor;
//											if (firesensor.getIsJumping(i) == "POSITIVE") {
//												Color[] colors2 = {
//														Color.DARK_GRAY,
//														Color.PINK };
//												RadialGradientPaint rgp = new RadialGradientPaint(
//														p, radius, dist,
//														colors2);
//												graphics2D.setPaint(rgp);
//												// }
//											} 
//											 if(firesensor.getIsJumping(i) == "NO") {
//												Color[] colors2 = {
//														Color.GRAY,
//														Color.DARK_GRAY };
//												RadialGradientPaint rgp = new RadialGradientPaint(
//														p, radius, dist,
//														colors2);
//												graphics2D.setPaint(rgp);
//											}	 if(firesensor.getIsJumping(i) == "NULL") {
//												Color[] colors2 = {
//														Color.BLUE,
//														Color.DARK_GRAY };
//												RadialGradientPaint rgp = new RadialGradientPaint(
//														p, radius, dist,
//														colors2);
//												graphics2D.setPaint(rgp);
//											}
//											
//
//										}
//										
//										
//										
//										
//										
////										if (coneSensor instanceof WallRaySensor) {
////
////											Color[] colors = { Color.DARK_GRAY,
////													Color.LIGHT_GRAY };
////											RadialGradientPaint rgp = new RadialGradientPaint(
////													p, radius, dist, colors);
////											graphics2D.setPaint(rgp);
////
////										}
//
////										if (coneSensor instanceof FireSensor) {
////
////											FireSensor firesensor = (FireSensor) coneSensor;
////											if (firesensor.getIsDisabled(i) == false) {
////												Color[] colors2 = {
////														Color.DARK_GRAY,
////														Color.PINK };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////												// }
////											} else {
////												Color[] colors2 = {
////														Color.DARK_GRAY,
////														Color.BLACK };
////												RadialGradientPaint rgp = new RadialGradientPaint(
////														p, radius, dist,
////														colors2);
////												graphics2D.setPaint(rgp);
////											}
////
////										}
//
//										if (paperSensors) {
//											graphics2D
//													.setColor(Color.LIGHT_GRAY);
//										}
//
//										graphics2D
//												.fillArc(
//														x3,
//														y3,
//														(int) FastMath
//																.round(cutOff
//																		* 2
//																		* scale),
//														(int) (FastMath
//																.round(cutOff
//																		* 2
//																		* scale)),
//														a1,
//														(int) FastMath.round(FastMath
//																.toDegrees(openingAngle)));
//
//									}
//
//									// graphics2D.setColor(Color.BLACK);
//									// graphics2D.drawArc(x2, y2,
//									// (int)FastMath.round(range*2*scale),
//									// (int)(FastMath.round(range*2*scale)), a1,
//									// (int)FastMath.round(FastMath.toDegrees(openingAngle)));
//									// graphics2D.drawLine(x1, y1, gx1, gy1);
//									// graphics2D.drawLine(x1, y1, gx2, gy2);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		}
//	}

	// @Override
	// protected void drawArea(Graphics g, Environment environment) {
	// // TODO Auto-generated method stub
	// super.drawArea(g, environment);
	//
	// Robot r=environment.getRobots().get(0);
	// // if(oldPosition!=null){
	// // g.drawLine(transformX(oldPosition.getX()), transformY(oldPosition.y),
	// // (int) transformX(oldPosition.distanceTo(r.getPosition())), (int)
	// transformY(oldPosition.distanceTo(r.getPosition())));
	// // }
	//
	// if(oldPosition!=null){
	// g.drawLine((int) transformX(oldPosition.x), (int)
	// transformY(oldPosition.y),
	// (int)transformX(r.getPosition().x), (int) transformY(r.getPosition().y));
	// }
	//
	// oldPosition=new Vector2d((int) r.getPosition().x,(int)
	// r.getPosition().y);
	//
	// }
	
	
	
	

}
