package renderer;

import java.awt.Color;
import java.awt.Graphics;

import mathutils.Vector2d;
import robots.JumpingRobot;
import robots.JumpingSumo;
import simulation.robot.Robot;
import simulation.util.Arguments;
import gui.renderer.TwoDRenderer;

public class RitaTwoDRenderer extends TwoDRenderer {

	public RitaTwoDRenderer(Arguments args) {
		super(args);
	}

	@Override
	protected void drawRobot(Graphics graphics, Robot robot) {
		if (image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();
		int circleDiameter = bigRobots ? (int) Math.max(10,
				Math.round(robot.getDiameter() * scale)) : (int) Math
				.round(robot.getDiameter() * scale);
		int x = (int) (transformX(robot.getPosition().getX()) - circleDiameter / 2);
		int y = (int) (transformY(robot.getPosition().getY()) - circleDiameter / 2);

		// if(robot.getId() == selectedRobot) {
		// graphics.setColor(Color.yellow);
		// graphics.fillOval(x-2, y-2, circleDiameter + 4, circleDiameter + 4);
		//
		// }

		// se saltar, passar cor para vermelho

		if(robot instanceof JumpingSumo && ((JumpingSumo) robot).isJumping()){
			if (((JumpingSumo)robot).isJumpingUp()) {
				robot.setBodyColor(Color.GREEN);
			} else {
				robot.setBodyColor(Color.ORANGE);
			}
			
		} else if (robot.isInvolvedInCollisonWall()) {
			robot.setBodyColor(Color.PINK);
		} else
			robot.setBodyColor(Color.BLACK);
		
		
		
		
		
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
	
}
