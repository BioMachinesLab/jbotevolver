package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PheromoneActuator;
import simulation.util.Arguments;

public class ArtificialPheromoneNNOutput implements NNOutput {

	private PheromoneActuator a;
	
	public ArtificialPheromoneNNOutput(Actuator a, Arguments args){
		this.a = (PheromoneActuator)a;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		
	}

	@Override
	public void apply() {
		
	}
}