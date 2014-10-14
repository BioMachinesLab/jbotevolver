package evaluationfunctions;

import sensors.SimplePreySharedSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import environment.SharedPreyForageEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SharedPreyForageEvaluationFunction extends EvaluationFunction {

	private double maxDistanceToForage;
	private int foodForaged = 0;
//	private double penalty;
	
	public SharedPreyForageEvaluationFunction(Arguments args) {
		super(args);
	}
	
	@Override
	public void update(Simulator simulator) {
		SharedPreyForageEnvironment environment = (SharedPreyForageEnvironment) simulator.getEnvironment();
		
		maxDistanceToForage = environment.getWidth();
		
		for(Robot r : environment.getRobots()) {
			double closestPreyDistanceToRobot = Double.MAX_VALUE;
			
			for(Prey p : environment.getPrey()) {
				double distanceToRobot = p.getPosition().distanceTo(r.getPosition());
				if(distanceToRobot < closestPreyDistanceToRobot) {
					closestPreyDistanceToRobot = distanceToRobot;
				}
			}
			
			SimplePreySharedSensor preySensor = (SimplePreySharedSensor)r.getSensorByType(SimplePreySharedSensor.class);
			double preySensorValue = 0;
			
			for (int i = 0; i < preySensor.getNumberOfSensors(); i++) {
				preySensorValue = Math.max(preySensorValue, preySensor.getSensorReading(i));
			}
			
			//Aproximar da presa
			if(closestPreyDistanceToRobot < Double.MAX_VALUE) {
				fitness += preySensorValue * .0000001;
			}

			//Trazer a presa para o ninho
			PreyPickerActuator picker = (PreyPickerActuator)r.getActuatorByType(PreyPickerActuator.class);
			
			if(picker.isCarryingPrey()){
				double distanceToNest = r.getPosition().distanceTo(environment.getNest().getPosition());
				fitness += (maxDistanceToForage - distanceToNest) / maxDistanceToForage * .00001;
			}
			
//			if(r.isInvolvedInCollison()){
//				if(picker.isCarryingPrey()){
//					picker.dropPrey();
//				}
//				penalty += 2/environment.getRobots().size()/environment.getSteps();
//			}
			
		}
		
		foodForaged = environment.getPreysCaught();
		
	}

	@Override
	public double getFitness() {
		return super.getFitness() + foodForaged;
	}
	
}