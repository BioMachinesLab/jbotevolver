package sensors;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import environment.SharedPreyForageEnvironment;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.physicalobjects.checkers.AllowPreyChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import utils.SensorEstimation;

public class SharedSensor extends ConeTypeSensor {

	private HashMap<Integer, Vector2d[]> targetsEstimations;
	private int numberOfRobots;
	
	public SharedSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowOrderedPreyChecker(robot.getId()));
		
		Arguments robotsArguments = simulator.getArguments().get("--robots");
		numberOfRobots = robotsArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
		targetsEstimations = new HashMap<Integer, Vector2d[]>();
	}
	
	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {

		Vector2d[] estimations = targetsEstimations.get(source.getObject().getId() - Simulator.maxNumberRobots);
		
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
			return (getRange() - sensorInfo.getDistance()) / getRange();
		}
		return 0;
	}
	
	
	public void addEstimation(int robotId, LinkedList<SensorEstimation> sensorEstimations) {
		for (SensorEstimation sE : sensorEstimations) {
			Vector2d[] estimations;
			
			if(targetsEstimations.get(sE.getTargetId()) == null){
				estimations = new Vector2d[numberOfRobots];
				estimations[robotId] = sE.getEstimation();
				targetsEstimations.put(sE.getTargetId(), estimations);
			}else{
				estimations = targetsEstimations.get(sE.getTargetId());
				estimations[robotId] = sE.getEstimation();
				targetsEstimations.put(sE.getTargetId(), estimations);
			}
			
		}

	}

	public void clearEstimations(int robotId, LinkedList<SensorEstimation> sensorEstimations) {
		for (SensorEstimation sE : sensorEstimations) {
			Vector2d[] estimations;
			if(targetsEstimations.get(sE.getTargetId()) != null){
				estimations = targetsEstimations.get(sE.getTargetId());
				estimations[robotId] = null;
				targetsEstimations.put(sE.getTargetId(), estimations);
			}
		}
		
	}
	
}
