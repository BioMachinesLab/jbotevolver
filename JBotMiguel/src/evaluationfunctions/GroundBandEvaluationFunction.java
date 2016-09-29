package evaluationfunctions;

import mathutils.Vector2d;
import sensors.GroundBandSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import environments.GroundBandEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class GroundBandEvaluationFunction extends EvaluationFunction{
	
	protected double rSignal = 0;
	protected double rDistance = 0;
	protected double dFurthest = 0;
	protected double dNearest = 0;
	
	protected Vector2d target;
	protected GroundBandEnvironment env;
	protected boolean hasSignalled = false;

	public GroundBandEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		
		Robot r = simulator.getRobots().get(0);
		
		if(simulator.getTime() == 0) {
			target = new Vector2d(0,0);
			dFurthest = new Vector2d(r.getPosition()).distanceTo(target);
			dNearest = dFurthest;
			env = (GroundBandEnvironment)simulator.getEnvironment();
		}
		
		rSignal = calculateSignalContribution(r);
		rDistance = calculateDistanceContribution(r);
		
		if(getGroundBandSensorReading(r) > 0.1 && !env.isOpen())
			dNearest = 0;
		
		if(checkForCrossing(r)) {
			rSignal = 0;
			rDistance = 0;
			simulator.stopSimulation();
		}
		
		if(dFurthest > 1.2) {
			rSignal = 0;
			rDistance = 0;
			simulator.stopSimulation();
		}
		
		fitness = rSignal + rDistance;
	}
	
	protected boolean checkForCrossing(Robot r) {
		 return getGroundBandSensorReading(r) > 0.8;
	}
	
	protected double getGroundBandSensorReading(Robot r) {
		GroundBandSensor s = (GroundBandSensor)r.getSensorByType(GroundBandSensor.class);
		return s.getSensorReading(0);
	}
	
	protected double calculateDistanceContribution(Robot r) {
		double currentDistance = r.getPosition().distanceTo(target);
		
		if(currentDistance < dNearest)
			dNearest = currentDistance;
		
		if(currentDistance > dFurthest) {
			dFurthest = currentDistance;
			dNearest = dFurthest;
		}
		
		if(dNearest < 0.075)
			dNearest = 0;
		
		return (dFurthest - dNearest)/dFurthest;
	}
	
	protected double calculateSignalContribution(Robot r) {
		if(!hasSignalled)
			hasSignalled = isSignalling(r);
		
		if(env.isOpen())
			return hasSignalled ? 0 : 1;
		else
			return hasSignalled ? 1 : 0;
	}
	
	protected boolean isSignalling(Robot r) {
		PreyPickerActuator act = (PreyPickerActuator) r.getActuatorByType(PreyPickerActuator.class);
		return act.getStatus() != PreyPickerActuator.PickerStatus.DROP;
	}
}
