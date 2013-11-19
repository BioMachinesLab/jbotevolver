package gui.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import mathutils.Vector2d;
import simulation.robot.Epuck;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class TwoDRendererDebug extends TwoDRenderer {

	protected int selectedRobot=-1;
	private boolean wallRay;
	private int coneSensorId;
	private String coneClass = "";

	public TwoDRendererDebug(Arguments args) {
		super(args);
		this.addMouseListener(new MouseListenerSentinel());
		wallRay = args.getArgumentAsIntOrSetDefault("wallray", 0)==1;
		coneSensorId = args.getArgumentAsIntOrSetDefault("conesensorid",-1);
		coneClass = args.getArgumentAsStringOrSetDefault("coneclass","");
		
		
	}
	
	protected void drawRobot(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();
		int circleDiameter = (int) Math.round(robot.getDiameter() * scale);
		int x = (int) (transformX(robot.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(robot.getPosition().getY()) - circleDiameter / 2);

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
		
		System.out.println("Robot ID: "+robot.getId());
		System.out.println("\n Actuators:");
		for(Actuator act:robot.getActuators()){
			System.out.println(act);
		}
		System.out.println("\n Sensors:");
		for(Sensor sensor:robot.getSensors()){
			System.out.println(sensor);
		}
		System.out.println("\n\n");
		
		if(robot instanceof Epuck) {
			Sensor s = robot.getSensorByType(EpuckIRSensor.class);
			if(s != null) {
				EpuckIRSensor ir = (EpuckIRSensor)s;
				drawLines(ir.rayPositions, graphics);
			}
		}
		
		if(wallRay){
			Sensor s = robot.getSensorByType(WallRaySensor.class);
			if(s != null) {
				WallRaySensor wall = (WallRaySensor)s;
				drawLines(wall.rayPositions, graphics);
			}
		}	
		
		if(coneSensorId >= 0 || !coneClass.isEmpty()){
			
			for(Sensor s : robot.getSensors())
				if(s.getClass().getSimpleName().equals(coneClass) || s.getId() == coneSensorId){
					if(s != null){
						ConeTypeSensor preySensor = (ConeTypeSensor)s;
						for (Double angle : preySensor.getAngles()) {
						
							double xi = robot.getPosition().getX()+robot.getRadius()*Math.cos(angle + robot.getOrientation());
							double yi = robot.getPosition().getY()+robot.getRadius()*Math.sin(angle + robot.getOrientation());
							
							double range = preySensor.getRange();
							double cutOff = preySensor.getCutOff();
							double openingAngle = preySensor.getOpeningAngle();

							int x1 = transformX(xi);
							int y1 = transformY(yi);
							
							int x2 = transformX(xi-range);
							int y2 = transformY(yi+range);
							
							int x3 = transformX(xi-cutOff);
							int y3 = transformY(yi+cutOff);
							
							int gx1 = transformX(robot.getPosition().getX()+(robot.getRadius()+range)*Math.cos(angle + robot.getOrientation()));
							int gy1 = transformY(robot.getPosition().getY()+(robot.getRadius()+range)*Math.sin(angle + robot.getOrientation()));
							
							int a1 = (int)(Math.round(Math.toDegrees(angle + robot.getOrientation() - openingAngle/2)));
							
							Graphics2D graphics2D = (Graphics2D) graphics.create();

							GradientPaint gp = new GradientPaint(x1, y1,Color.darkGray , gx1, gy1, Color.lightGray, false);
							graphics2D.setColor(Color.BLACK);
							graphics2D.fillArc(x2, y2, (int)Math.round(range*2*scale), (int)(Math.round(range*2*scale)), a1, (int)Math.round(Math.toDegrees(openingAngle)));   
							
							graphics2D.setPaint(gp);
							graphics2D.fillArc(x3, y3, (int)Math.round(cutOff*2*scale), (int)(Math.round(cutOff*2*scale)), a1, (int)Math.round(Math.toDegrees(openingAngle)));
							
						}
					}
				}
		}
			
			
		
	}

	public int getSelectedRobot() {
		return selectedRobot;
	}

	public class MouseListenerSentinel implements MouseListener {

		//		@Override
		public void mouseClicked(MouseEvent e) {
			for (Robot robot : simulator.getEnvironment().getRobots()) {
				int circleDiameter = (int) Math.round(0.5 + robot.getDiameter() * scale);
				int x1 = (int) (transformX(robot.getPosition().getX()) - circleDiameter / 2);
				int x2 = (int) (transformX(robot.getPosition().getX()) + circleDiameter / 2);
				int y1 = (int) (transformY(robot.getPosition().getY()) - circleDiameter / 2);
				int y2 = (int) (transformY(robot.getPosition().getY()) + circleDiameter / 2);

				if(e.getX() > x1 && e.getX() < x2 &&
						e.getY() > y1 && e.getY() < y2){
					selectedRobot = robot.getId();
					return;
				}
			}
			selectedRobot = -1;
		}

		//		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		//		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		//		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		//		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
		}
	}
}