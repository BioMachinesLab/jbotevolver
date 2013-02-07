package gui.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import mathutils.Vector2d;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class TwoDRendererDebug extends TwoDRenderer {

	protected int selectedRobot=-1;

	public TwoDRendererDebug(Arguments args) {
		super(args);
		this.addMouseListener(new MouseListenerSentinel());
	}

	protected void drawRobot(Graphics graphics, Robot robot) {
		if(robot.getId() == selectedRobot) {
			int circleDiameter = (int) Math.round(0.5 + robot.getDiameter() * scale);
			int x = (int) (transformX(robot.getPosition().getX()) - circleDiameter / 2);
			int y = (int) (transformY(robot.getPosition().getY()) - circleDiameter / 2);

			graphics.setColor(Color.yellow);
			graphics.fillOval(x-2, y-2, circleDiameter + 4, circleDiameter + 4);
			int cx = transformX( robot.getPosition().getX());
			int cy = transformY(robot.getPosition().getY());
			double orientation  = robot.getOrientation();
			Vector2d p0 = new Vector2d();
			for(Sensor sensor : robot.getSensors()){ 
				if (sensor.getClass().isInstance(ConeTypeSensor.class)) {
					ConeTypeSensor coneSensor = (ConeTypeSensor) sensor;
					if(coneSensor.getId() < 3){
						if (sensor.getId() == 2)
							graphics.setColor(Color.black);

						double[] angles = coneSensor.getAngles();
						for(int i=0; i < coneSensor.getNumberOfSensors(); i++){
							p0.set(sensor.getSensorReading(i) + robot.getRadius(), 0);
							p0.rotate(orientation+angles[i]);
							x=transformX(p0.getX() + robot.getPosition().getX());
							y=transformY(p0.getY() + robot.getPosition().getY());
							graphics.drawLine(cx, cy, x, y);
						}
					}
				}
			}
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

		}
		super.drawRobot(graphics, robot);
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