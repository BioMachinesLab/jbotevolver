package renderer;

import environment.OpenEnvironment;
import environment.OpenEnvironment2;
import gui.renderer.TwoDRendererDebug;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import sensors.DistanceToASensor;
import sensors.DistanceToBSensor;
import sensors.TypeBRobotSensor;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;

public class InesTwoDRenderer extends TwoDRendererDebug {

	private boolean drawArea;
	private boolean coevolution;
	
	public InesTwoDRenderer(Arguments args) {
		super(args);
		drawArea = args.getArgumentAsIntOrSetDefault("drawarea", 0)==1;
	}

	@Override
	protected void drawArea(Graphics g, Environment environment) {
		
		if(drawArea){
			
			OpenEnvironment2 env = (OpenEnvironment2) environment;
			//OpenEnvironment2 env = (OpenEnvironment2) environment;
			//OpenEnvironment env = (OpenEnvironment) environment;
			
			ArrayList<Robot> typeA = env.getTypeARobots();
			ArrayList<Robot> typeB = env.getTypeBRobots();

			ArrayList<Robot> temp = new ArrayList<Robot>();
			for(Robot b: typeB){
				temp.add(b);
			}

			for(Robot robot: typeB) {
				DistanceToBSensor sensorB = (DistanceToBSensor) robot.getSensorByType(DistanceToBSensor.class);
				DistanceToASensor sensorA = (DistanceToASensor) robot.getSensorByType(DistanceToASensor.class);
				for(Robot r: env.getRobots()) {
					if(!r.equals(robot) && r.getDescription().equals("type1") && 
							r.getPosition().distanceTo(robot.getPosition()) <= sensorB.getRange() - robot.getRadius()) {								
						g.setColor(Color.BLUE);
						g.drawLine((int) transformX(r.getPosition().x), (int) transformY(r.getPosition().y), 
								(int) transformX(robot.getPosition().x), (int) transformY(robot.getPosition().y));

					} else if(r.getDescription().equals("type0") &&
							r.getPosition().distanceTo(robot.getPosition()) <= sensorA.getRange() - robot.getRadius()){
						g.setColor(Color.BLACK);
						g.drawLine((int) transformX(r.getPosition().x), (int) transformY(r.getPosition().y), 
								(int) transformX(robot.getPosition().x), (int) transformY(robot.getPosition().y));
					}
				}
			}

			double xmax = - Double.MAX_VALUE;
			double xmin = Double.MAX_VALUE;
			double ymax = - Double.MAX_VALUE;
			double ymin = Double.MAX_VALUE;
			boolean infinity = true;

			if(env.isConnected()){
				for(Robot b: typeB) {
					for(Robot a: typeA){
						if (a.getPosition().distanceTo(b.getPosition()) <= ((DistanceToBSensor) a.getSensorByType(DistanceToBSensor.class)).getRange() - a.getRadius()){

							infinity = false;

							if (a.getPosition().x < xmin)
								xmin = a.getPosition().x;

							if (a.getPosition().y < ymin)
								ymin = a.getPosition().y;

							if (a.getPosition().x > xmax)
								xmax = a.getPosition().x;

							if (a.getPosition().y > ymax)
								ymax = a.getPosition().y;	

						}

					}
				}

				if(!infinity){
					int areaXMin = (int) transformX(xmin);
					int areaYMin = (int) transformY(ymax);
					int areaXMax = (int) transformX(xmax);
					int areaYMax = (int) transformY(ymin);
					
//					double areaWidth = Math.abs(xmax-xmin) ;
//					double areaHeight = Math.abs(ymax-ymin);
					int areaWidth = (int) Math.abs(areaXMax-areaXMin) ;
					int areaHeight = (int) Math.abs(areaYMax-areaYMin);

					g.setColor(Color.BLACK);
//					g.drawRect(areaXMin, areaYMin, areaWidth, areaHeight);
				}
			}
		}
	}
	
}
