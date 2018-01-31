package outputs;

import actuator.JumpActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpNNOutput extends NNOutput {
	protected JumpActuator actuactor;
	
	public JumpNNOutput(Actuator actuactor, Arguments args){
		super(actuactor, args);
		this.actuactor=(JumpActuator)actuactor;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if(value >0.5){
			actuactor.jump();
		}
	}

	@Override
	public void apply() {
				
	}

}
