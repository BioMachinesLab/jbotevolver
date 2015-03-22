package evaluationfunctions;

import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.SharedSensorForageEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SharedSensorsEvaluationFunction extends EvaluationFunction  {

	private int foodForaged = 0;
	private SharedSensorForageEnvironment environment;
	private double numberOfPredators = 0;
	
	public SharedSensorsEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		environment = (SharedSensorForageEnvironment) simulator.getEnvironment();
		foodForaged = environment.getPreysCaught();
		
		for(Robot r : environment.getRobots()) {
			if(!r.getDescription().equals("prey")){
				IntruderSensor intruder = (IntruderSensor)r.getSensorByType(IntruderSensor.class);
				double sensorValue = 0;
				
				for (int i = 0; i < intruder.getNumberOfSensors(); i++) {
					sensorValue = Math.max(sensorValue, intruder.getSensorReading(i));
				}
				
				fitness += sensorValue * 0.00001;
				
			}
		}
		
		if(numberOfPredators != environment.getCurrentNumberOfPredators()){
			numberOfPredators = environment.getCurrentNumberOfPredators();
		}
		
	}
	
	@Override
	public double getFitness() {
//		System.out.println(numberOfPredators);
		if(numberOfPredators == 0){
			return super.getFitness() + foodForaged;
		}else{
			return (super.getFitness() + foodForaged)/environment.getCurrentNumberOfPredators();
		}
		
	}
	
}
