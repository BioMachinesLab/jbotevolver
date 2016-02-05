package gui.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.collisionhandling.knotsandbolts.PolygonShape;
import simulation.robot.LedState;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class TwoDRendererDebug extends TwoDRenderer {

	protected int selectedRobot=-1;
	private boolean wallRay;
	private int coneSensorId;
	private String coneClass = "";
	private int robotId;
	private boolean boardSensors;
	private boolean paperSensors;

	private boolean blink = true;
	
	private Vector2d selectedLocation;
	
	public TwoDRendererDebug(Arguments args) {
		super(args);
		this.addMouseListener(new MouseListenerSentinel());
		wallRay = args.getArgumentAsIntOrSetDefault("wallray", 0)==1;
		coneSensorId = args.getArgumentAsIntOrSetDefault("conesensorid",-1);
		coneClass = args.getArgumentAsStringOrSetDefault("coneclass","");
		robotId = args.getArgumentAsIntOrSetDefault("robotid",-1);
		boardSensors = args.getArgumentAsIntOrSetDefault("boardsensors", 0)==1;
		paperSensors = args.getArgumentAsIntOrSetDefault("papersensors", 0)==1;
	}
	
	protected void drawLines(Vector2d[][][] positions, Graphics graphics) {
		if(positions != null) {
			for(int i = 0 ; i < positions.length ; i++) {
				for(int j = 0 ; j < positions[i].length ; j++) {
					if(i % 2 == 0) {
						graphics.setColor(Color.RED);
					}else {
						graphics.setColor(Color.BLACK);
					}
					
					if(positions[i][j][0] != null) {
						int x1 = transformX(positions[i][j][0].x);
						int y1 = transformY(positions[i][j][0].y);
						int x2 = transformX(positions[i][j][1].x);
						int y2 = transformY(positions[i][j][1].y);
						graphics.drawLine(x1, y1, x2, y2);
					}
				}
			}
		}
	}
	
	@Override
	public void drawWall(Wall w) {
		super.drawWall(w);
		
		graphics.setColor(Color.RED);
		PolygonShape s = (PolygonShape)w.shape;
		Polygon p = s.getPolygon();
		
		int[] xs = p.xpoints.clone();
		int[] ys = p.ypoints.clone();
		
		for(int i = 0 ; i < xs.length ; i++) {
			double x = xs[i]/10000.0;
			double y = ys[i]/10000.0;
			xs[i] = transformX(x);
			ys[i] = transformY(y);
		}
		
		Polygon e2 = new Polygon(xs,ys,4);
		Graphics2D g2 = (Graphics2D) graphics;
		
		g2.draw(e2);
		
		if(s.collision) {
			g2.fill(e2);
			s.collision = false;
		}
	}
	
	@Override
	protected void drawRobot(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();
		int circleDiameter = bigRobots ? (int)Math.max(10,Math.round(robot.getDiameter() * scale)) : (int) Math.round(robot.getDiameter() * scale);
		int x = transformX(robot.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(robot.getPosition().getY()) - circleDiameter / 2;

//		if(robot.getId() == selectedRobot) {
//			graphics.setColor(Color.yellow);
//			graphics.fillOval(x-2, y-2, circleDiameter + 4, circleDiameter + 4);
//			
//		}
		graphics.setColor(robot.getBodyColor());
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		
		int avgColor = (robot.getBodyColor().getRed()+robot.getBodyColor().getGreen()+robot.getBodyColor().getBlue())/3;
		
		if (avgColor > 255/2) {
			graphics.setColor(Color.BLACK);
		} else {
			graphics.setColor(Color.WHITE);
		}

		double orientation  = robot.getOrientation();
		Vector2d p0 = new Vector2d();
		Vector2d p1 = new Vector2d();
		Vector2d p2 = new Vector2d();
		p0.set( 0, -robot.getRadius() / 3);
		p1.set( 0, robot.getRadius() / 3);
		p2.set( 6 * robot.getRadius() / 7, 0);

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
		
//		System.out.println("Robot ID: "+robot.getId());
//		System.out.println("\n Actuators:");
//		for(Actuator act:robot.getActuators()){
//			System.out.println(act);
//		}
//		System.out.println("\n Sensors:"); 
//		for(Sensor sensor:robot.getSensors()){
//			System.out.println(sensor);
//		}
//		System.out.println("\n\n");
		
		double ledRadius = 0.015;
		
		p0.set(ledRadius*3/2,0);
		p0.rotate(orientation + Math.PI);
		
		int ledX = transformX(p0.getX() + robot.getPosition().getX() - ledRadius);
		int ledY = transformY(p0.getY() + robot.getPosition().getY() + ledRadius);
		
		int leadDiameter = (int) Math.round(ledRadius * 2 *scale);
		
		boolean paint = false;
		
		if(robot.getLedState() == LedState.BLINKING){
			if(blink){
				robot.setLedColor(Color.RED);
				graphics.setColor(robot.getLedColor());
				paint = true;
				blink = false;
			}else{
				blink = true;
			}
			
		}else if(robot.getLedState() == LedState.ON){
			robot.setLedColor(Color.RED);
			graphics.setColor(robot.getLedColor());
			paint = true;
		}
		if(paint)
			graphics.fillOval(ledX, ledY, leadDiameter, leadDiameter);
		
		if(wallRay){
			
			Sensor s = robot.getSensorByType(WallRaySensor.class);
			
			if(s == null) {
				for(Sensor sensor : robot.getSensors()) {
					if(WallRaySensor.class.isAssignableFrom(sensor.getClass())) {
						s = sensor;
						break;
					}
				}
			}
			
			if(s != null) {
				WallRaySensor wall = (WallRaySensor)s;
				if(wall.rayPositions != null)
					drawLines(wall.rayPositions, graphics);
			}
		}
		
	}

	@Override
	protected void drawCones(Graphics graphics, Robot robot){
		if(robotId != -1 && robot.getId() != robotId)
			return;
		if(!robot.getDescription().equals("prey")){
			if(coneSensorId >= 0 || !coneClass.isEmpty()){	
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
									Point2D p = new Point2D.Double(x1, y1);
									float radius = (float) (cutOff*scale);
									float[] dist = {0.0f, 1.0f};
									Color[] colors = {Color.DARK_GRAY, Color.LIGHT_GRAY};
									RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
									graphics2D.setPaint(rgp);
									
									if(paperSensors)
										graphics2D.setColor(Color.LIGHT_GRAY);
									
									graphics2D.fillArc(x3, y3, (int)FastMath.round(cutOff*2*scale), (int)(FastMath.round(cutOff*2*scale)), a1, (int)FastMath.round(FastMath.toDegrees(openingAngle)));
								}
								
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
	
	@Override
	protected void drawNest(Graphics graphics2, Nest nest) {
		int circleDiameter = (int) Math.round(0.5 + nest.getDiameter() * scale);
		int x = transformX(nest.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(nest.getPosition().getY()) - circleDiameter / 2;

		if(paperSensors)
			graphics2.setColor(Color.GRAY.darker());
		else
			graphics2.setColor(nest.getColor());
		
		graphics2.fillOval(x, y, circleDiameter, circleDiameter);
		graphics2.setColor(Color.BLACK);
		
	}
	
	@Override
	public int getSelectedRobot() {
		return selectedRobot;
	}
	
	protected double screenToSimulationX(double x) {
		return 1*((x - centerX)/scale - horizontalMovement);
	}

	protected double screenToSimulationY(double y) {
		return -1*((y - centerY)/scale - verticalMovement);
	}

	public class MouseListenerSentinel implements MouseListener {

		//		@Override
		@Override
		public void mouseClicked(MouseEvent e) {
			selectedLocation = new Vector2d(screenToSimulationX(e.getX()),screenToSimulationY(e.getY()));
			System.out.println(selectedLocation);
			for (Robot robot : simulator.getEnvironment().getRobots()) {
				int circleDiameter = (int) FastMath.round(0.5 + robot.getDiameter() * scale);
				int x1 = transformX(robot.getPosition().getX()) - circleDiameter / 2;
				int x2 = transformX(robot.getPosition().getX()) + circleDiameter / 2;
				int y1 = transformY(robot.getPosition().getY()) - circleDiameter / 2;
				int y2 = transformY(robot.getPosition().getY()) + circleDiameter / 2;

				if(e.getX() > x1 && e.getX() < x2 &&
						e.getY() > y1 && e.getY() < y2){
					selectedRobot = robot.getId();
					return;
				}
			}
			selectedRobot = -1;
		}

		//		@Override
		@Override
		public void mouseEntered(MouseEvent e) {
		}

		//		@Override
		@Override
		public void mouseExited(MouseEvent e) {
		}

		//		@Override
		@Override
		public void mousePressed(MouseEvent e) {
		}

		//		@Override
		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}
	
	public Vector2d getSelectedLocation() {
		return selectedLocation;
	}
	
	public void clearSelectedLocation() {
		this.selectedLocation = null;
	}
}