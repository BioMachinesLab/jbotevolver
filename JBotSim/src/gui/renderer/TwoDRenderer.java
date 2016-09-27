package gui.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.environment.Environment;
import simulation.physicalobjects.GroundBand;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.Marker;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.Wall.Edge;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoDRenderer extends Renderer
		implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener {

	protected static final long serialVersionUID = -1376516458026928095L;
	protected BufferedImage image;
	protected Graphics graphics;
	protected double scale;
	protected double centerX;
	protected double centerY;
	protected double horizontalMovement = 0;
	protected double verticalMovement = 0;

	protected double zoomFactor = 1.0;

	protected boolean bigRobots = false;

	private boolean debug = false;

	protected double drawFrames = 1;
	protected boolean darIds;

	private int px;
	private int py;
	private boolean clicked = false;

	public TwoDRenderer(Arguments args) {
		super(args);
		this.addComponentListener(this);
		createImage();
		bigRobots = args.getArgumentAsIntOrSetDefault("bigrobots", 0) == 1;
		drawFrames = args.getArgumentAsIntOrSetDefault("drawframes", 1);
		darIds = args.getArgumentAsIntOrSetDefault("drawids", 1) == 1;
	}

	public double getScale() {
		return scale;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		synchronized (this) {
			if (image != null)
				g2.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, this);
		}
	}

	@Override
	public synchronized void drawFrame() {
		if (simulator == null) {
			graphics.setColor(new Color(0xEE, 0xEE, 0xEE));
			graphics.fillRect(0, 0, getWidth(), getHeight());
			return;
		}
		
		if (simulator.getTime() % drawFrames != 0) {
			// repaint();
			return;
		}

		drawBackground();

		if (simulator.getEnvironment().getAllObjects().size() > 0) {

			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				switch (m.getType()) {
				case NEST:
					drawNest(graphics, (Nest) m);
					break;
				case LIGHTPOLE:
					drawLightPole(graphics, (LightPole) m);
					break;
				case MARKER:
					drawMarker(graphics, (Marker) m);
					break;
				default:
					break;
				}

				if (m instanceof GroundBand) {
					drawGroundBand((GroundBand) m);
				}
			}

			int numberOfRobots = 0;
			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				switch (m.getType()) {
				case PREY:
					drawPreys(graphics, (Prey) m);
					break;
				case ROBOT:
					drawCones(graphics, (Robot) m);
					numberOfRobots++;
					break;
				case WALLBUTTON:
					drawWall((Wall) m);
					break;
				case WALL:
					drawWall((Wall) m);
					break;
				case LINE:
					drawLine((Line) m);
					break;
				default:
					break;
				}
			}

			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				switch (m.getType()) {
				case ROBOT:
					drawEntities(graphics, (Robot) m);
					drawRobot(graphics, (Robot) m);
					if (numberOfRobots > 1 && darIds)
						drawRobotId(graphics, (Robot) m);
				default:
					break;
				}
			}
		}

		drawArea(graphics, simulator.getEnvironment());
		drawTitle(graphics);

		repaint();
	}

	protected void drawMarker(Graphics g, Marker m) {

		int markerSize = (int) (scale * m.getRadius());
		double markerLength = m.getLength();

		int x = transformX(m.getPosition().x);
		int y = transformY(m.getPosition().y);

		g.setColor(m.getColor());

		if (m.isSquare()) {
			g.drawRect(x - markerSize / 2, y - markerSize / 2, markerSize, markerSize);
		} else
			g.drawOval(x - markerSize / 2, y - markerSize / 2, markerSize, markerSize);

		double orientation = m.getOrientation();
		Vector2d endPoint = new Vector2d(m.getPosition());
		endPoint.add(new Vector2d(markerLength * Math.cos(orientation), markerLength * Math.sin(orientation)));

		int x2 = transformX(endPoint.x);
		int y2 = transformY(endPoint.y);

		g.drawLine(x, y, x2, y2);
	}

	protected void drawEntities(Graphics graphics2, Robot m) {

	}

	protected void drawCones(Graphics g, Robot robot) {

	}

	protected void drawArea(Graphics g, Environment environment) {

	}

	protected void drawRobotId(Graphics g, Robot robot) {

		int x = transformX(robot.getPosition().x + robot.getRadius() + (bigRobots ? 1 : 0));
		int y = transformY(robot.getPosition().y - robot.getDiameter() - (bigRobots ? 1 : 0));

		g.setColor(Color.WHITE);
		g.fillRect(x, y - 10, 8, 10);

		g.setColor(Color.BLACK);
		g.drawString(String.valueOf(robot.getId()), x, y);
	}

	@Override
	public void zoomIn() {
		zoomFactor *= 1.1;
	}

	@Override
	public void zoomOut() {
		zoomFactor /= 1.11;
	}

	@Override
	public void resetZoom() {
		zoomFactor = 1.0;
	}

	public void drawBackground() {
		int width = image.getWidth();
		int height = image.getHeight();
		//
		double envWidth = simulator.getEnvironment().getWidth();
		double envHeight = simulator.getEnvironment().getHeight();
		//
		double scaleX = width / envWidth * zoomFactor;
		double scaleY = height / envHeight * zoomFactor;
		scale = scaleX;
		if (scaleX > scaleY)
			scale = scaleY;

		scale *= 0.9;

		centerX = width / 2.0;
		centerY = height / 2.0;

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		graphics.setColor(Color.WHITE);

		double w = (simulator.getEnvironment().getWidth());
		double h = (simulator.getEnvironment().getHeight());

		graphics.fillRect(transformX(-w / 2.0), transformY(h / 2.0), (int) (w * scale), (int) (h * scale));
	}

	public void drawGroundBand(GroundBand gb) {

		double xi = gb.getPosition().getX();
		double yi = gb.getPosition().getY();

		double outerRadius = gb.getOuterRadius();
		double innerRadius = gb.getInnerRadius();
		int endOrientation = (int) Math.toDegrees(gb.getEndOrientation());
		int startOrientation = (int) Math.toDegrees(gb.getStartOrientation());

		float diff = (float) (innerRadius / outerRadius);

		int outerWidth = (int) (outerRadius * 2 * scale);
		int innerWidth = (int) (innerRadius * 2 * scale);

		int totalOrientation = endOrientation - startOrientation;

		Graphics2D graphics2D = (Graphics2D) graphics.create();

		Point2D p = new Point2D.Double(transformX(xi), transformY(yi));
		float radius = outerWidth / 2;
		float[] dist = { 0.0f, diff, 1.0f };
		Color[] colors = { Color.BLACK, Color.BLACK, Color.WHITE };
		if (radius > 0) {
			RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);

			graphics2D.setPaint(rgp);
			graphics2D.fillArc(transformX(xi - outerRadius), transformY(yi + outerRadius), outerWidth, outerWidth,
					startOrientation, totalOrientation);

			graphics2D.setColor(Color.WHITE);
			graphics2D.fillOval(transformX(xi - innerRadius), transformY(yi + innerRadius), innerWidth, innerWidth);
		}

	}

	public void drawWall(Wall w) {
		
		graphics.setColor(w.color);

		Edge[] edges = w.getEdges();

		// for(int i = 0 ; i < edges.length ; i++) {
		// if(i==3)
		// graphics.drawLine(
		// (int)(transformX(edges[i].getP1().x)),
		// (int)(transformY(edges[i].getP1().y)),
		// (int)(transformX(edges[i].getP2().x)),
		// (int)(transformY(edges[i].getP2().y))
		// );
		// }

		int[] xs = new int[4];
		xs[0] = transformX(edges[3].getP1().x);
		xs[1] = transformX(edges[1].getP1().x);
		xs[2] = transformX(edges[2].getP1().x);
		xs[3] = transformX(edges[0].getP1().x);

		int[] ys = new int[4];
		ys[0] = transformY(edges[3].getP1().y);
		ys[1] = transformY(edges[1].getP1().y);
		ys[2] = transformY(edges[2].getP1().y);
		ys[3] = transformY(edges[0].getP1().y);

		graphics.fillPolygon(xs, ys, 4);

		// int wallWidth = (int) (m.getWidth() * scale);
		// int wallHeight = (int) (m.getHeight()* scale);
		// int x = (int) transformX(m.getTopLeftX());
		// int y = (int) transformY(m.getTopLeftY());
		//
		// graphics.fillRect(x, y, wallWidth, wallHeight);
		
		graphics.setColor(Color.BLACK);

	}

	// @Override
	@Override
	public void dispose() {
	}

	public void drawCircle(Point2d center, double radius) {
		int circleDiameter = (int) Math.round(0.5 + 2 * radius * scale);
		int x = transformX(center.getX()) - circleDiameter / 2;
		int y = transformY(center.getY()) - circleDiameter / 2;

		graphics.setColor(Color.LIGHT_GRAY);
		graphics.drawOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);

	}

	protected void drawLine(Line l) {
		int x0 = transformX(l.getPointA().getX());
		int x1 = transformX(l.getPointB().getX());

		int y0 = transformY(l.getPointA().getY());
		int y1 = transformY(l.getPointB().getY());

		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setStroke(new BasicStroke(5.0f));
		graphics.setColor(l.getColor());
		graphics.drawLine(x0, y0, x1, y1);
		graphics.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1.0f));
	}

	protected void drawNest(Graphics graphics2, Nest nest) {
		int circleDiameter = (int) Math.round(0.5 + nest.getDiameter() * scale);
		int x = transformX(nest.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(nest.getPosition().getY()) - circleDiameter / 2;

		graphics.setColor(nest.getColor());
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);

	}

	protected void drawLightPole(Graphics graphics, LightPole lightPole) {
		int circleDiameter = (int) Math.round(0.5 + lightPole.getDiameter() * scale);

		int x = transformX(lightPole.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(lightPole.getPosition().getY()) - circleDiameter / 2;

		if (lightPole.isTurnedOn()) {
			graphics.setColor(lightPole.getColor());
			graphics.fillOval(x, y, circleDiameter, circleDiameter);
		}
		graphics.setColor(Color.BLACK);
		graphics.fillOval(x + circleDiameter / 2 - 1, y + circleDiameter / 2 - 1, 2, 2);
		graphics.setColor(Color.BLACK);
		drawLightPoleId(graphics, lightPole);

	}

	protected void drawLightPoleId(Graphics g, LightPole lp) {
		int x = transformX(lp.getPosition().x + lp.getRadius());
		int y = transformY(lp.getPosition().y - lp.getRadius());

		g.setColor(Color.WHITE);
		g.fillRect(x, y - 10, 8, 10);

		g.setColor(Color.BLACK);
		g.drawString(String.valueOf(lp.getName().substring(lp.getName().length() - 2)), x, y);
	}

	protected void drawPreys(Graphics graphics, Prey prey) {

		int circleDiameter = (int) Math.round(0.5 + prey.getDiameter() * scale);
		int x = transformX(prey.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(prey.getPosition().getY()) - circleDiameter / 2;

		if (prey.isEnabled()) {
			graphics.setColor(prey.getColor());
		} else {
			graphics.setColor(Color.gray);
		}
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.BLACK);
	}

	protected void drawRobot(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();

		int circleDiameter = bigRobots ? (int) Math.max(10, Math.round(robot.getDiameter() * scale))
				: (int) Math.round(robot.getDiameter() * scale);
		int x = transformX(robot.getPosition().getX()) - circleDiameter / 2;
		int y = transformY(robot.getPosition().getY()) - circleDiameter / 2;

		// if(robot.getId() == selectedRobot) {
		// graphics.setColor(Color.yellow);
		// graphics.fillOval(x-2, y-2, circleDiameter + 4, circleDiameter + 4);
		//
		// }
		graphics.setColor(robot.getBodyColor());
		graphics.fillOval(x, y, circleDiameter, circleDiameter);

		int avgColor = (robot.getBodyColor().getRed() + robot.getBodyColor().getGreen()
				+ robot.getBodyColor().getBlue()) / 3;

		if (avgColor > 255 / 2) {
			graphics.setColor(Color.BLACK);
		} else {
			graphics.setColor(Color.RED);
		}

		double orientation = robot.getOrientation();
		Vector2d p0 = new Vector2d();
		Vector2d p1 = new Vector2d();
		Vector2d p2 = new Vector2d();

		p0.set(0, -robot.getDiameter() / 4);
		p1.set(0, robot.getDiameter() / 4);
		p2.set(robot.getDiameter() / 2, 0);

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

	public int transformX(double x) {
		return (int) Math.round((x - horizontalMovement) * scale + centerX);
	}

	public int transformY(double y) {
		return (int) Math.round((-y + verticalMovement) * scale + centerY);
	}

	// @Override
	@Override
	public void componentResized(ComponentEvent arg0) {
		createImage();
		drawFrame();
	}

	// @Override
	@Override
	public void componentHidden(ComponentEvent arg0) {
		createImage();
		drawFrame();
	}

	// @Override
	@Override
	public void componentMoved(ComponentEvent arg0) {
		createImage();
		drawFrame();
	}

	// @Override
	@Override
	public void componentShown(ComponentEvent arg0) {
		createImage();
		drawFrame();
	}

	public synchronized void createImage() {
		if (getWidth() == 0 || getHeight() == 0) {
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		} else if (getWidth() > 0 && getHeight() > 0) {
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		}
		graphics = image.getGraphics();
	}

	public int getSelectedRobot() {
		return -1;
	}

	@Override
	public void moveLeft() {
		horizontalMovement -= 0.1 / scale * 100;
		componentResized(null);
	}

	@Override
	public void moveRight() {
		horizontalMovement += 0.1 / scale * 100;
		componentResized(null);
	}

	@Override
	public void moveUp() {
		verticalMovement += 0.1 / scale * 100;
		componentResized(null);
	}

	@Override
	public void moveDown() {
		verticalMovement -= 0.1 / scale * 100;
		componentResized(null);
	}

	/*
	 * Interfaces implementation
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			clicked = true;
			px = e.getX();
			py = e.getY();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			// int dx = e.getX() - px;
			// int dy = e.getY() - py;
			// verticalMovement += dy / scale;
			// horizontalMovement -= dx / scale;
			// componentResized(null);
			clicked = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (clicked) {
			int dx = e.getX() - px;
			int dy = e.getY() - py;
			px = e.getX();
			py = e.getY();
			verticalMovement += dy / scale * 1.5;
			horizontalMovement -= dx / scale * 1.5;
			componentResized(null);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for (int i = 0; i < Math.abs(e.getWheelRotation()); i++) {
			if (e.getWheelRotation() < 0) {
				zoomIn();
			} else {
				zoomOut();
			}
		}
		componentResized(null);
	}
}