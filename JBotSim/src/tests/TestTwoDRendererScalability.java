package tests;

import gui.renderer.TwoDRenderer;

import java.awt.Color;
import java.util.Random;

import javax.swing.JFrame;

import mathutils.Vector2d;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.checkers.AllowPreyChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.RobotColorSensor;
import simulation.util.SimRandom;

public class TestTwoDRendererScalability {

	private static final int NUMBER_PREYS = 13;
	private static final int FRAMEFREQUENCY = 10;
	private static final int STEPS = 6000 * 30;
	public static final int        NUMBEROFROBOTS = 500;
	public static final double     ENVIRONMENTSIZE = 100;
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Sim");
		frame.setSize(800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Simulator s = new Simulator(new SimRandom());
		
		Environment e = new Environment(s,ENVIRONMENTSIZE, ENVIRONMENTSIZE);
		s.setEnvironment(e);
//		e.addMovableObject(new Nest("Nest", 0, 0, .5));
		Random random = new Random();
		
		for (int i = 0; i < NUMBEROFROBOTS; i++) {
			Robot r = new Robot(s,"testbot" + i, 
								(random.nextDouble() - 0.5) * ENVIRONMENTSIZE * 0.8, 
								(random.nextDouble() - 0.5) * ENVIRONMENTSIZE * 0.8, 
								0, 0, 0.50,0.50, null);
			Vector2d[] positions = new Vector2d[1];
			positions[0] = new Vector2d(1,0);
//			LightTypeSensor frontSensor = new LightTypeSensor(0, r, s, positions, Math.PI/4.0, 1, new AllowPreyChecker(r.getId()));
//			r.addSensor(frontSensor);
//			RobotColorSensor colorSensor = new RobotColorSensor(1, r, s, positions, Math.PI/4.0, Color.RED,2);
//			r.addSensor(colorSensor);
			e.addRobot(r);		
			r.setWheelSpeed(random.nextDouble() * MovableObject.MAXIMUMSPEED, random.nextDouble() * MovableObject.MAXIMUMSPEED);
		}	
		
		TwoDRenderer render = new TwoDRenderer(s);
		frame.getContentPane().add(render.getComponent());
		frame.setVisible(true);
		
		Robot r = new Robot(s,"testbot2", 1, -1, 0, 0, 1,1, null);
		e.addRobot(r);	
		r.setBodyColor(Color.RED);
		r.setWheelSpeed(MovableObject.MAXIMUMSPEED, MovableObject.MAXIMUMSPEED/2);
		for(int i=0;i<NUMBER_PREYS;i++){
			e.addPrey( new Prey(s,"prey"+i, 
					(random.nextDouble() - 0.5) * ENVIRONMENTSIZE * 0.8, 
					(random.nextDouble() - 0.5) * ENVIRONMENTSIZE * 0.8, 
					0, 1,0.10));
		}		
//		e.addPrey( new Prey("prey1", -1, 1, 0, 1,0.10));
//		
//		e.addPrey( new Prey("prey1", -1, -1, 0, 1,0.10));
		
		double time = 0;
		long startTime = System.currentTimeMillis();
		while (time < STEPS) {
			if (time % FRAMEFREQUENCY == 0) render.drawFrame();
			s.performOneSimulationStep(time++);
			if (time % 200 == 0)
				System.out.println("Step: " + time);
			//System.out.println(colorSensor + ", " + frontSensor);
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
		}
		System.out.println("Time spent: " + (double) (System.currentTimeMillis() - startTime) / 1000 + " seconds for " + STEPS + " steps"); 
	}
}
