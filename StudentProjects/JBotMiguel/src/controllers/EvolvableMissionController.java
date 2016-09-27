package controllers;

import environments.MaritimeMissionEnvironment;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import sensors.IntruderSensor;
import sensors.ParameterSensor;
import sensors.RobotPositionSensor;
import sensors.WaypointSensor;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class EvolvableMissionController extends PreprogrammedArbitrator implements FixedLenghtGenomeEvolvableController {

	private enum State {
		GO_TO_AREA,PATROL
	}
	
	private enum Controllers {
		WAYPOINT,PATROL
	}
	
	private State currentState = State.GO_TO_AREA;
	private String destination;
	private String base;
	private WaypointSensor waypointSensor;
	private MaritimeMissionEnvironment env;
	private Simulator simulator;
	private RobotPositionSensor rps;
	private DifferentialDriveRobot r;
	
	public EvolvableMissionController(Simulator simulator, Robot robot,
			Arguments args) {
		super(simulator, robot, args);
		destination = args.getArgumentAsString("destination");
		this.simulator = simulator;
		rps = (RobotPositionSensor) robot.getSensorByType(RobotPositionSensor.class);
		r = (DifferentialDriveRobot)robot;
	}
	
	@Override
	public void controlStep(double time) {
		if(env == null) {
			env = (MaritimeMissionEnvironment)simulator.getEnvironment();
			waypointSensor = (WaypointSensor)robot.getSensorByType(WaypointSensor.class);
		}
		
		int subController = 0;
		
		switch(currentState) {
			case GO_TO_AREA:
				waypointSensor.setDestination(destination);
				subController = Controllers.WAYPOINT.ordinal();
				
				if(waypointSensor.getSensorReading(0) > 0) {
					currentState = State.PATROL;
				}
				
				break;
			case PATROL:
				subController = Controllers.PATROL.ordinal();
				break;
		}
		chooseSubController(subController,time);
	}

	@Override
	public void setNNWeights(double[] weights) {
		((NeuralNetworkController)subControllers.get(0)).setNNWeights(weights);
	}

	@Override
	public int getGenomeLength() {
		return ((NeuralNetworkController)subControllers.get(0)).getGenomeLength();
	}

	@Override
	public double[] getNNWeights() {
		return ((NeuralNetworkController)subControllers.get(0)).getNNWeights();
	}
	@Override
	public int getNumberOfInputs() {
		return 0;
	}
	
	@Override
	public int getNumberOfOutputs() {
		return 0;
	}
}