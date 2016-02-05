package gui.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

@SuppressWarnings("serial")
public class TraceRenderer extends Renderer {
	
	public static final int SCREEN = 0;
	public static final int JPG    = 1;
	public static final int PNG    = 2;

	int terminal  = PNG;
	
	int traceRobotSteps = 450;
	int tracePreySteps  = 50;
	
	BufferedImage image;
	Graphics      graphics;
	protected Simulator simulator;
	
	double scale;
	double centerX;
	double centerY;
	
	int outputPeriod 					= 10;
	int simulationFramesSinceLastOutput = -1;
	int outputFrameNumber               = 0;
	
	int imageSizeX = 3000;
	int imageSizeY = 3000;
	
	String filenameBase = "frame";
	
	protected class TracePoints extends Vector<Point2d> {
		private int currentPosition = 0;
		private int maxLength;
		
		public TracePoints(int maxLength) {
			super(maxLength);
			this.maxLength = maxLength;
		}
		
		public void addPoint(Point2d newPoint) {
			if (currentPosition == maxLength) {
				currentPosition = 0;
			}
			
			if (currentPosition  == size()) {
				add(new Point2d());
			}
			
			Point2d oldPoint = elementAt(currentPosition);
			oldPoint.set(newPoint.x, newPoint.y);
			currentPosition++;
		}
	}
	
	HashMap<Robot, TracePoints> robotTraces;
	HashMap<Prey,  TracePoints> preyTraces;
	
	public TraceRenderer(Arguments args) {
		super(args);
		robotTraces = new HashMap<Robot, TracePoints>();
		preyTraces  = new HashMap<Prey,  TracePoints>();

		image    = new BufferedImage(imageSizeX, imageSizeY, BufferedImage.TYPE_INT_RGB);
		graphics = image.getGraphics();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, this);
	}	
	
	@Override
	public void drawFrame() {
		if(simulator.getEnvironment().getMovableObjects().size() > 0) {
			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				switch(m.getType()) {
				case PREY:
					TracePoints preyTrace;
					if (preyTraces.containsKey(m)) {
						preyTrace = preyTraces.get(m);
					} else {
						preyTrace = new TracePoints(tracePreySteps); 
						preyTraces.put((Prey) m, preyTrace);
					}					
					preyTrace.addPoint(((Prey) m).getPosition());
					break;
				case ROBOT:
					TracePoints robotTrace;
					if (robotTraces.containsKey(m)) {
						robotTrace = robotTraces.get(m);
					} else {
						robotTrace = new TracePoints(traceRobotSteps); 
						robotTraces.put((Robot) m, robotTrace);
					}					
					robotTrace.addPoint(((Robot) m).getPosition());
					break;
				}
			}
		}

		if (simulationFramesSinceLastOutput == outputPeriod - 1 || simulationFramesSinceLastOutput == -1)  {
			outputFrame(outputFrameNumber++);
			simulationFramesSinceLastOutput = 0;
		} else {
			simulationFramesSinceLastOutput++;			
		}
	}
	
	public void outputFrame(int frameNumber) {		
		int width  = image.getWidth();
		int height = image.getHeight();

		double envWidth  =  simulator.getEnvironment().getWidth()*1.5;
		double envHeight =  simulator.getEnvironment().getHeight()*1.5;

		double scaleX = width  / envWidth;
		double scaleY = height / envHeight;
		scale  = scaleX;
		if (scaleX > scaleY)
			scale = scaleY;

		scale *= 0.9;

		centerX = width / 2.0;
		centerY = height / 2.0;

		graphics.setColor(Color.GRAY);
		graphics.fillRect(0, 0, width, height);		
		graphics.setColor(Color.WHITE);
		graphics.fillRect(
				(int) ((- envWidth / 2.0 * scale) + centerX), 
				(int) ((- envHeight /2.0  * scale) + centerY),
				(int) (( envWidth * scale)), 
				(int) (( envHeight * scale)));

		 simulator.getEnvironment().draw(this);
		
		if( simulator.getEnvironment().getMovableObjects().size()>0){
			for (PhysicalObject m :  simulator.getEnvironment().getAllObjects()) {
				switch(m.getType()){
				case ROBOT:
					graphics.setColor(new Color(150, 150, 150));
					
					drawTrace(graphics, robotTraces.get(m));
					drawRobot(graphics, (Robot) m);
					break;
					
				case PREY:
					graphics.setColor(new Color(180, 180, 180));

					drawTrace(graphics, preyTraces.get(m));
					drawPrey(graphics, (Prey) m);
					break;
				
				case NEST:
					drawNest(graphics, (Nest) m);
					break;
				}
			}		
		}

		if (terminal == PNG) {
			try {
				ImageIO.write(image, "png", new File(filenameBase + frameNumber + ".png"));
			} catch (IOException e) {
				System.out.println("Could not save file: " + filenameBase + frameNumber + ".png, reason: " + e.getMessage());
				e.printStackTrace();
			}
		}

		if (terminal == JPG) {
			try {
				ImageIO.write(image, "png", new File(filenameBase + frameNumber + ".jpg"));
			} catch (IOException e) {
				System.out.println("Could not save file: " + filenameBase + frameNumber + ".jpg, reason: " + e.getMessage());
				e.printStackTrace();
			}
		}
		repaint();
	}

	private void drawNest(Graphics graphics, Nest nest) {
		int circleDiameter = (int) Math.round(0.5 + nest.getDiameter() * scale);
		int x = transformX(nest.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(nest.getPosition().getY()) - circleDiameter / 2;

		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);
		
	}

	private void drawPrey(Graphics graphics, Prey prey) {
		int circleDiameter = (int) Math.round(0.5 + prey.getDiameter() * scale);
		int x = transformX(prey.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(prey.getPosition().getY()) - circleDiameter / 2;

		if(prey.isEnabled()){
			graphics.setColor(Color.CYAN);
		} else {
			graphics.setColor(Color.gray);
		}
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);


	}


	protected void drawRobot(Graphics graphics, Robot robot) {
		int circleDiameter = (int) Math.round(0.5 + robot.getDiameter() * scale);
		int x = transformX(robot.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(robot.getPosition().getY()) - circleDiameter / 2;

		graphics.setColor(robot.getBodyColor());
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		if (robot.getBodyColor() != Color.BLACK) {
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
	}

	protected int transformX(double x) {
		return (int) (x * scale + centerX);
	}

	protected int transformY(double y) {
		return (int) (-y * scale + centerY);
	}
	
	protected void drawTrace(Graphics g, TracePoints tp) {
		for (Point2d p : tp) {
			int imageX = transformX(p.x);
			int imageY = transformY(p.y);
			
			g.drawRect(imageX, imageY, 1, 1);
		}
	}

	@Override
	public void resetZoom() {
		
	}

	@Override
	public void zoomIn() {
		
	}

	@Override
	public void zoomOut() {
	}

	@Override
	public void dispose() {
		
	}
}
