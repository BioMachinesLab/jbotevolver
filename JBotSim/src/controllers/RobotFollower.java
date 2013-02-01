package controllers;

import java.util.Iterator;

import simulation.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.behaviors.Behavior;
import simulation.robot.behaviors.MoveForwardBehavior;
import simulation.robot.behaviors.TurnLeftBehavior;
import simulation.robot.behaviors.TurnRightBehavior;
import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.NearRobotSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class RobotFollower extends Controller{
	
	private NearRobotSensor nrs;
	private EpuckIRSensor ir;
	private boolean waiting = true;
	private MoveForwardBehavior forward;
	private Behavior left;
	private Behavior right;
	private int activeBehavior = 0;

	public RobotFollower(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot);
		
//		forward = new MoveForwardBehavior(simulator, robot, false);
//		left = new TurnLeftBehavior(simulator, robot, true);
//		right = new TurnRightBehavior(simulator, robot, true);
//		
//		Iterator<Sensor> iterator = robot.getSensors().iterator();
//		
//		boolean foundSensor = false;
//		
//		while(iterator.hasNext() && !foundSensor) {
//			Sensor s = iterator.next();
//			if(s instanceof NearRobotSensor)
//				nrs = (NearRobotSensor)s;
//			if(s instanceof EpuckIRSensor)
//				ir = (EpuckIRSensor)s;
//		}
	}
	
	@Override
	public void controlStep(int time) {
		
//		double reading = nrs.getSensorReading(0);
//		double threshold = 0.5;
//		
//		if(waiting)
//			waiting = reading == 0;
//		
//		if(!waiting) {
//			
//			if(right.isLocked()) {
//				right.applyBehavior();
//			} else if(left.isLocked()) {
//				left.applyBehavior();
//			} else {
//				robot.setWheelSpeed(0,0);
//				
//				System.out.println("not waiting "+ir.getSensorReading(0)+" "+ir.getSensorReading(1)+"    "+ir.getSensorReading(2)+" "+ir.getSensorReading(3));
//				
//				if(reading == 0) {
//					
//					if(ir.getSensorReading(0) < threshold && ir.getSensorReading(3) < threshold) {
//						forward.applyBehavior();
//					}
//					if(ir.getSensorReading(1) == 0 && ir.getSensorReading(2) > 0) {
//						right.applyBehavior();
//						System.out.println("right");
//					} else if(ir.getSensorReading(2) == 0 && ir.getSensorReading(1) > 0) {
//						left.applyBehavior();
//						System.out.println("left");
//					}
//				}
//			}
//		}
	}
}
