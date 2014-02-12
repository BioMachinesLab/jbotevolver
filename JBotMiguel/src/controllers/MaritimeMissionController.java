package controllers;

import environments.MaritimeMissionEnvironment;
import sensors.ParameterSensor;
import sensors.WaypointSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MaritimeMissionController extends PreprogrammedArbitrator {

	private enum State {
		GO_TO_AREA,PATROL,RETURN_TO_BASE
	}
	
	private State currentState = State.GO_TO_AREA;
	private String destination;
	private String base;
	private WaypointSensor waypointSensor;
	private ParameterSensor energySensor;
	private MaritimeMissionEnvironment env;
	private Simulator simulator;
	
	private double energyRecharge = 1/(1*10000);
	private double energyLost = 1/(100*1000);
	
	public MaritimeMissionController(Simulator simulator, Robot robot,
			Arguments args) {
		super(simulator, robot, args);
		destination = args.getArgumentAsString("destination");
		base = args.getArgumentAsString("base");
		this.simulator = simulator;
	}
	
	@Override
	public void controlStep(double time) {
		
		if(env == null)
			env = (MaritimeMissionEnvironment)simulator.getEnvironment();
		
		int subController = 0;
		
		updateEnergy();
		
		if(waypointSensor == null)
			waypointSensor = (WaypointSensor)robot.getSensorByType(WaypointSensor.class);
		if(energySensor == null)
			energySensor = (ParameterSensor)robot.getSensorByType(ParameterSensor.class);
//		if(robot.getId() == 1)
//			System.out.println(destination);
		switch(currentState) {
			case GO_TO_AREA:
				waypointSensor.setDestination(destination);
				subController = 0;
				
				if(waypointSensor.getSensorReading(0) > 0.5) {
					currentState = State.PATROL;
				}
				
				break;
			case PATROL:
				subController = 1;
				
				if(energySensor.getSensorReading(0) < 0.15) {
					currentState = State.RETURN_TO_BASE;
				}
				
				break;
			case RETURN_TO_BASE:
				waypointSensor.setDestination(base);
				subController = 0;
				
				if(energySensor.getSensorReading(0) >= 1) {
					currentState = State.GO_TO_AREA;
				}
				
				break;
		}
		chooseSubController(subController,time);
	}
	
	private void updateEnergy() {
		ParameterSensor s = (ParameterSensor)robot.getSensorByType(ParameterSensor.class);
		
		double value = s.getCurrentValue();
		
		if(robot.getPosition().distanceTo(env.base.getPosition()) < 5) {
			s.setCurrentValue(Math.min(value + energyRecharge, 1.0));
		} else {
			s.setCurrentValue(Math.max(value - energyLost, 0.0));
			
			if(s.getCurrentValue() == 0) {
				robot.setEnabled(false);
			}
		}
	}
}