package evaluationfunctions;

import sensors.ThymioIRSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import actuator.ThymioTwoWheelActuator;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ObstacleAvoidanceEvaluationFunction extends EvaluationFunction {
	
	private boolean penalty;
	
	public ObstacleAvoidanceEvaluationFunction(Arguments args) {
		super(args);
		penalty = args.getArgumentAsIntOrSetDefault("penalty", 0)==1;
	}

	@Override
	public void update(Simulator simulator) {
		for (Robot robot : simulator.getRobots()) {
			ThymioTwoWheelActuator wheels = (ThymioTwoWheelActuator)robot.getActuatorByType(ThymioTwoWheelActuator.class);
			double[] percentages = wheels.getSpeedPrecentage();
			
			ThymioIRSensor sensor = (ThymioIRSensor)robot.getSensorByType(ThymioIRSensor.class);
			double readings = sensor.getSensorReading(2);
			
			if(penalty){
				fitness -= (1 - (5000 - readings) / 5000) * 0.05;	
			}
			 
			double p = percentages[0]+percentages[1];
			fitness+=p/100;
			
			if(robot.isInvolvedInCollison()){
				fitness = -1;
				simulator.stopSimulation();
			}
			
		}
		
		fitness/=simulator.getRobots().size();
		
	}

	@Override
	public double getFitness() {
		return fitness;
	}
	
}
