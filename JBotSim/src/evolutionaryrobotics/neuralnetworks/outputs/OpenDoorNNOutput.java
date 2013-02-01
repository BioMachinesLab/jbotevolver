package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.OpenDoorActuator;
import simulation.util.Arguments;

public class OpenDoorNNOutput implements NNOutput {
	private OpenDoorActuator openDoor;
	private boolean open = false;
	
	public OpenDoorNNOutput(Actuator openDoorActuator, Arguments args) {
		this.openDoor = (OpenDoorActuator)openDoorActuator;
	}

//	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

//	@Override
	public void setValue(int index, double value) {
		open = value > 0.5;
	}

	@Override
	public void apply() {
		openDoor.open(open);
	}
}