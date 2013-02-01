package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.PheromoneSensor;
import simulation.robot.sensors.Sensor;

public class PheromoneNNInput extends NNInput {
	
	PheromoneSensor pS;
	
	public PheromoneNNInput (Sensor pS){
		super();
		this.pS = (PheromoneSensor) pS;
	}

	@Override
	public int getNumberOfInputValues() {
		
		return pS.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {		
		return pS.getSensorReading(index);
	}

}
