package controllers;

import java.awt.Color;

import mathutils.Vector2d;
import environments.MaritimeMissionEnvironment;
import sensors.IntruderSensor;
import sensors.ParameterSensor;
import sensors.RobotPositionSensor;
import sensors.WaypointSensor;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MaritimeMissionController extends PreprogrammedArbitrator {

	public enum State {
		GO_TO_AREA,PATROL,RECHARGE,DETECT_INTRUDER
	}
	
	public enum Controllers {
		PATROL,INTRUDER,WAYPOINT
	}
	
	private State currentState = State.GO_TO_AREA;
	private String destination = "";
	private LightPole base;
	private WaypointSensor waypointSensor;
	private IntruderSensor intruderSensor;
	private ParameterSensor energySensor;
	private MaritimeMissionEnvironment env;
	private Simulator simulator;
	private RobotPositionSensor rps;
	private DifferentialDriveRobot r;
	
	private double energyRecharge = .0055556;
	private double energyLostFullThrottle = 0.0017361;
	private double energyLostStandby = 0.000277778;
	private double goBackEnergy = 0;
	private int lastIntruderTime = 0;
	private int alertTimeout = 5*60*10; //5min
	private double alertDistance = 200;
	
	public MaritimeMissionController(Simulator simulator, Robot robot,
			Arguments args) {
		super(simulator, robot, args);
		destination = args.getArgumentAsStringOrSetDefault("destination","");
		alertTimeout = args.getArgumentAsIntOrSetDefault("alerttimeout", alertTimeout);
		alertDistance = args.getArgumentAsDoubleOrSetDefault("alertdistance", alertDistance);
		this.simulator = simulator;
		rps = (RobotPositionSensor) robot.getSensorByType(RobotPositionSensor.class);
//		destination = ""+(robot.getId()%9);
		r = (DifferentialDriveRobot)robot;
		r.setInvisible(true);
	}
	
	@Override
	public void controlStep(double time) {
		if(env == null) {
			env = (MaritimeMissionEnvironment)simulator.getEnvironment();
			waypointSensor = (WaypointSensor)robot.getSensorByType(WaypointSensor.class);
			energySensor = (ParameterSensor)robot.getSensorByType(ParameterSensor.class);
			intruderSensor = (IntruderSensor)robot.getSensorByType(IntruderSensor.class);
			energySensor.setCurrentValue(100);
			base = env.getBases().get(robot.getId()%env.getNumberOfBases());
		}
		
		int subController = 0;
		
		updateEnergy();
		
		if(time % 1000 == 0) {
			double d = robot.getPosition().distanceTo(base.getPosition()) / simulator.getEnvironment().getMaxApproximationSpeed();
			
			goBackEnergy = (d*energyLostFullThrottle/simulator.getTimeDelta())*1.2;
		}
		
		if(energySensor.getCurrentValue() < goBackEnergy) {
			currentState = State.RECHARGE;
			robot.setInvisible(true);
		}
		
		switch(currentState) {
			case GO_TO_AREA:
				r.setBodyColor(Color.BLACK);
				if(destination.isEmpty()) {
					destination = env.getRandomWaypointLeft(base.getPosition().getX() < 0 ? true : false);
				}
				waypointSensor.setDestination(destination);
				subController = Controllers.WAYPOINT.ordinal();
				
				if(waypointSensor.getSensorReading(0) > 0) {
					currentState = State.PATROL;
					env.removeWaypoint(destination);
					destination = "";
					robot.setInvisible(false);
				}
				
				break;
			case PATROL:
				r.setBodyColor(Color.GREEN);
				subController = Controllers.PATROL.ordinal();
				
				if(intruderSensor.foundIntruder()) {
					currentState = State.DETECT_INTRUDER;
				}
				
				break;
			case DETECT_INTRUDER:
				r.setBodyColor(Color.RED);
				subController = Controllers.INTRUDER.ordinal();
				
				if(intruderSensor.foundIntruder()) {
					lastIntruderTime = simulator.getTime().intValue();
					alertNearbyDrones();
				} else if(simulator.getTime() - lastIntruderTime > alertTimeout) {
					currentState = State.PATROL;
				}
				
				break;
			case RECHARGE:
				r.setBodyColor(Color.YELLOW);
				waypointSensor.setDestination(base.getName());
				subController = Controllers.WAYPOINT.ordinal();
				
				if(energySensor.getCurrentValue() >= 100) {
					currentState = State.GO_TO_AREA;
				} else {
					if(robot.getPosition().distanceTo(base.getPosition()) < base.getRadius()) {
						r.stop();
						return;
					}
				}
				
				break;
		}
		chooseSubController(subController,time);
	}
	
	private void alert() {
		lastIntruderTime = simulator.getTime().intValue();
		currentState = State.DETECT_INTRUDER;
	}
	
	private void alertNearbyDrones() {
		for(Robot r : simulator.getRobots()) {
			if(r.getController() instanceof MaritimeMissionController) {
				if(r.getPosition().distanceTo(robot.getPosition()) < alertDistance) {
					MaritimeMissionController mmc = (MaritimeMissionController)r.getController();
					mmc.alert();
				}
			}
		}
	}
	
	private void updateEnergy() {
		double value = energySensor.getCurrentValue();
		
		if(robot.getPosition().distanceTo(base.getPosition()) < env.baseRadius) {
			energySensor.setCurrentValue(Math.min(value + energyRecharge, 100));
		} else {
			value-=energyLostStandby;
			
			double l = Math.abs(r.getLeftWheelSpeed());
			double rr = Math.abs(r.getRightWheelSpeed());
			double speed=(l+rr)/2;
			double maxSpeed = 2.77;
			speed = speed/maxSpeed;
			
			value-=speed*energyLostFullThrottle;
			energySensor.setCurrentValue(Math.max(value, 0.0));
			
			if(energySensor.getCurrentValue() == 0) {
				robot.setEnabled(false);
			}
		}
	}
	
	public State getCurrentState() {
		return currentState;
	}
}