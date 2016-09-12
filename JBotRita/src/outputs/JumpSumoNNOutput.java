package outputs;


import actuator.JumpSumoActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpSumoNNOutput extends NNOutput {
	private JumpSumoActuator actuactor;

	public JumpSumoNNOutput(Actuator actuactor, Arguments args){
		super(actuactor, args);
		this.actuactor=( JumpSumoActuator) actuactor;
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

