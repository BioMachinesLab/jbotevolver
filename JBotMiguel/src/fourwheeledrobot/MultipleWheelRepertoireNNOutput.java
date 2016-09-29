package fourwheeledrobot;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class MultipleWheelRepertoireNNOutput extends NNOutput {
	
	private MultipleWheelRepertoireActuator actuator;
	
	double heading;
	double speed;
	
	public MultipleWheelRepertoireNNOutput(Actuator actuator, Arguments args) {
		super(actuator,args);
		this.actuator  = (MultipleWheelRepertoireActuator)actuator;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		if(output == 0) heading = value;
		if(output == 1) speed = value;
	}
	
	@Override
	public void apply() {
		actuator.setHeading(heading);
		actuator.setWheelSpeed(speed);
	}
}