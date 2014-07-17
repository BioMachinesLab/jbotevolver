package sensors;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class SharedStateSensor extends ConeTypeSensor {

	private Simulator simulator;
	private int numberOfRobots;
	private Prey[] preys;
	private HashMap<Integer, Vector2d[]> intrudersEstimations;
	private int robotOneSeeing = 0;
	
	public SharedStateSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		setAllowedObjectsChecker(new AllowMouseChecker(id));
		
		Arguments robotsArguments = simulator.getArguments().get("--robots");
		numberOfRobots = robotsArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
		intrudersEstimations = new HashMap<Integer, Vector2d[]>();
		
		preys = new Prey[numberOfRobots];
		
	}
	
	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {

		Vector2d[] estimations = intrudersEstimations.get(source.getObject().getId());
		
		if(estimations != null) {
		
			int robotsSeeingIntruder = 0;
			Vector2d est = null;
			for (Vector2d value : estimations) {
				if(value != null) {
					robotsSeeingIntruder++;
					est = value;
				}
			}
			
			if (robotsSeeingIntruder == 1) {
				return calculateValue(i, est);
			} else if(robotsSeeingIntruder > 1){
				return calculateValue(i, source.getObject().getPosition()); //posicao da presa
			}
		}
		
		return 0;
		
	}

	private double calculateValue(int i, Vector2d source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		if ((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
//			if(robot.getId() == 1){
//				robot.setBodyColor(Color.green);
//				robotOneSeeing ++;
//				System.out.println(robotOneSeeing);
//			}
			return (getRange() - sensorInfo.getDistance()) / getRange();
		}
		return 0;
	}
	
	public void addEstimation(int robotId, int preyId, Vector2d estimation) {
		Vector2d[] estimations;
		
		if(intrudersEstimations.get(preyId) == null){
			estimations = new Vector2d[numberOfRobots];
			estimations[robotId] = estimation;
			intrudersEstimations.put(preyId, estimations);
		}else{
			estimations = intrudersEstimations.get(preyId);
			estimations[robotId] = estimation;
			intrudersEstimations.put(preyId, estimations);
		}

	}

	public void clearEstimations(int robotId, int preyId) {
		Vector2d[] estimations;
		
		if(intrudersEstimations.get(preyId) != null){
			estimations = intrudersEstimations.get(preyId);
			estimations[robotId] = null;
			intrudersEstimations.put(preyId, estimations);
		}
		
	}
	
//	@Override
//	public void update(double time, ArrayList<PhysicalObject> teleported) {
//		if(robot.getId() == 1){
//			robotOneSeeing = 0;
//			robot.setBodyColor(Color.yellow);
//		}
//		super.update(time, teleported);
//		
//	}
	
}
