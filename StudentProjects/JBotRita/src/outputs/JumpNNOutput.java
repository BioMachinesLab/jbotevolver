package outputs;

import actuator.JumpActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

public class JumpNNOutput extends NNOutput {
	private JumpActuator actuactor;
	
	
	public JumpNNOutput(Actuator actuactor, Arguments args){
		super(actuactor, args);
		this.actuactor=(JumpActuator)actuactor;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		// TODO Auto-generated method stub
		if (value > 0.5) {
			actuactor.jumpStatus();
		}
		else{
//			System.out.println("é para o status tar off");
			actuactor.offStatus();
		}
		
	}

	@Override
	public void apply() {

	}

}
