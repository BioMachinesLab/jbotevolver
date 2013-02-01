package gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;

public class TwoDRenderer extends Component implements Renderer, ComponentListener {

	private static final long serialVersionUID = -1376516458026928095L;

	Simulator     simulator;
	BufferedImage image;
	Graphics      graphics;
	double        scale;
	double        centerX;
	double        centerY;
	Image		  bgImage;

	private double zoomFactor = 1.0;
	

	public TwoDRenderer(Simulator simulator) {
		this.simulator = simulator;
		this.addComponentListener(this);
		createImage();
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		synchronized (this) {
			if (image != null)
				g2.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, this);
		}
	}	

//	@Override
	public synchronized void drawFrame() {
		int width  = image.getWidth();
		int height = image.getHeight();

		double envWidth  = simulator.getEnvironment().getWidth();
		double envHeight = simulator.getEnvironment().getHeight();

		double scaleX = width  / envWidth    * zoomFactor;
		double scaleY = height / envHeight   * zoomFactor ; 
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
				(int) ((-simulator.getEnvironment().getWidth() / 2.0 * scale) + centerX), 
				(int) ((-simulator.getEnvironment().getHeight() /2.0  * scale) + centerY),
				(int) ((simulator.getEnvironment().getWidth() * scale)), 
				(int) ((simulator.getEnvironment().getHeight() * scale)));

		simulator.getEnvironment().draw(this);
		
		if(bgImage != null)
		{
			drawBackgroundImage(graphics, bgImage);
		}
		
		if(simulator.getEnvironment().getMovableObjects().size()>0){
			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				switch(m.getType()){
				case NEST:
					drawNest(graphics, (Nest)m);
					break;
				case PREY:
					drawPreys(graphics, (Prey)m);
					break;
				case ROBOT:
					drawRobot(graphics, (Robot) m);
					break;
				case LIGHTPOLE:
					drawLightPole(graphics, (LightPole)m);
					break;
				case WALL:
					drawWall((Wall) m);
					break;
				case WALLBUTTON:
					drawWallButton((Wall) m);
					break;
				}
			}	
			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				switch(m.getType()){
				case PREY:
					drawPreys(graphics, (Prey)m);
					break;
				}
			}		

		}
		repaint();
	}

	public void zoomIn() {
		zoomFactor *= 1.1;
	}
	
	public void zoomOut() {
		zoomFactor /= 1.11;
	}
	
	public void resetZoom() {
		zoomFactor = 1.0;
	}

    public void drawWall(Wall m) {
    	//System.out.println("draw wall");
    	
		graphics.setColor(Color.blue);
		
    	int wallWidth = (int) (m.getWidth() * scale);
		int wallHeight = (int) (m.getHeight()* scale);
		int x = (int) transformX(m.getTopLeftX());
		int y = (int) transformY(m.getTopLeftY());
		
		graphics.fillRect(x, y, wallWidth, wallHeight);
		graphics.setColor(Color.BLACK);
	}

	//	@Override
	public void dispose() {
	}

	public void setSimulator(Simulator simulator) {	
		this.simulator = simulator;
	}

	public void drawCircle(Point2d center, double radius) {
		int circleDiameter = (int) Math.round(0.5 + 2 * radius * scale);
		int x = (int) (transformX(center.getX()) - circleDiameter / 2);
		int y = (int) (transformY(center.getY()) - circleDiameter / 2);

		graphics.setColor(Color.LIGHT_GRAY);
		graphics.drawOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);	
		
	}
	
	private void drawNest(Graphics graphics2, Nest nest) {
		int circleDiameter = (int) Math.round(0.5 + nest.getDiameter() * scale);
		int x = (int) (transformX(nest.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(nest.getPosition().getY()) - circleDiameter / 2);

		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);
		
	}
	
	private void drawLightPole(Graphics graphics, LightPole lightPole){
		int circleDiameter = (int) Math.round(0.5 + lightPole.getDiameter() * scale);
		int x = (int) (transformX(lightPole.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(lightPole.getPosition().getY()) - circleDiameter / 2);
		
		if(lightPole.isTurnedOn()) {
			graphics.setColor(Color.YELLOW);
			graphics.fillOval(x, y, circleDiameter, circleDiameter);
			graphics.setColor(Color.YELLOW);
		}
		
		graphics.setColor(Color.BLACK);
		graphics.fillOval(x+circleDiameter/2-3, y+circleDiameter/2-3, 6, 6);
		graphics.setColor(Color.BLACK);
		
	}
	
	private void drawBackgroundImage(Graphics graphics, Image img) {
		
		int w = (int)(simulator.getEnvironment().getWidth()*scale);
		int h = (int)(simulator.getEnvironment().getHeight()*scale);
		
		
		int x = (int)(transformX(0) - w / 2);
		int y = (int)(transformY(0) - h / 2);
		
		graphics.drawImage(img, x, y, w, h, null);
	}

	private void drawPreys(Graphics graphics, Prey prey) {

		int circleDiameter = (int) Math.round(0.5 + prey.getDiameter() * scale);
		int x = (int) (transformX(prey.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(prey.getPosition().getY()) - circleDiameter / 2);

		if(prey.isEnabled()){
			graphics.setColor(Color.CYAN);
		} else {
			graphics.setColor(Color.gray);
		}
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);

//		double orientation  = prey.getOrientation();
//		Vector2d p0 = new Vector2d();
//		Vector2d p1 = new Vector2d();
//		Vector2d p2 = new Vector2d();
//		p0.set( 0, -prey.getRadius() / 3);
//		p1.set( 0, prey.getRadius() / 3);
//		p2.set( 6 * prey.getRadius() / 7, 0);
//
//		p0.rotate(orientation);
//		p1.rotate(orientation);
//		p2.rotate(orientation);
//
//		int[] xp = new int[3];
//		int[] yp = new int[3];
//
//		xp[0] = transformX(p0.getX() + prey.getPosition().getX());
//		yp[0] = transformY(p0.getY() + prey.getPosition().getY());
//
//		xp[1] = transformX(p1.getX() + prey.getPosition().getX());
//		yp[1] = transformY(p1.getY() + prey.getPosition().getY());
//
//		xp[2] = transformX(p2.getX() + prey.getPosition().getX());
//		yp[2] = transformY(p2.getY() + prey.getPosition().getY());
//
//		graphics.fillPolygon(xp, yp, 3);
		
		
		//graphics.drawString(prey.getId()+"", x, y);

	}


	protected void drawRobot(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();
		
		int circleDiameter = (int) Math.round(0.5 + robot.getDiameter() * scale);
		int x = (int) (transformX(robot.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(robot.getPosition().getY()) - circleDiameter / 2);

//		if(robot.getId() == selectedRobot) {
//			graphics.setColor(Color.yellow);
//			graphics.fillOval(x-2, y-2, circleDiameter + 4, circleDiameter + 4);
//			
//		}
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
//		graphics.drawString(robot.getId()+"", x, y);
	}
	

	private void drawWallButton(Wall m) {
		graphics.setColor(Color.RED);
		
    	int wallWidth = (int) (m.getWidth() * scale);
		int wallHeight = (int) (m.getHeight()* scale);
		int x = (int) transformX(m.getTopLeftX());
		int y = (int) transformY(m.getTopLeftY());
		
		graphics.fillRect(x, y, wallWidth, wallHeight);
		graphics.setColor(Color.BLACK);
	}

	protected int transformX(double x) {
		return (int) (x * scale + centerX);
	}

	protected int transformY(double y) {
		return (int) (-y * scale + centerY);
	}

//	@Override
	public void componentResized(ComponentEvent arg0) {
		createImage();
	}

//	@Override
	public void componentHidden(ComponentEvent arg0) {
		createImage();
	}

//	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

//	@Override
	public void componentShown(ComponentEvent arg0) {
		createImage();
	}

	protected synchronized void createImage() {
		if (getWidth() == 0 || getHeight() == 0) {
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);			
		} else 
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		graphics = image.getGraphics();
	}

//	@Override
	public Component getComponent() {
		return this;
	}

	public int getSelectedRobot() {
		return -1;
	}
	
	@Override
	public void drawImage(Image image) {
		this.bgImage = image;
	}
}
