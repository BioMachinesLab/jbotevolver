package outputs;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuator.ChangeSensorActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class ChangeSensorNNOutput extends NNOutput {
	
	private ChangeSensorActuator changeSensorActuator;
	private double range = 0;
	private double openingAngle = 0;
	private double orientation = 0;

	public ChangeSensorNNOutput(Actuator actuator, Arguments args) {
		super(actuator, args);
		changeSensorActuator = (ChangeSensorActuator)actuator;
	}

	@Override
	public int getNumberOfOutputValues() {
		return changeSensorActuator.getNumberOfOutputs();
	}

	@Override
	public void setValue(int index, double value) {
		if(index == 0 && changeSensorActuator.isChangeRange())
			range = value;
		else if(index <= 1 && changeSensorActuator.isChangeAngle())
			openingAngle = value;
		else
			orientation = value;
	}
	
	
	@Override
	public void apply() {
		if(changeSensorActuator.isChangeRange())
			changeSensorActuator.setRange(range);
		
		if(changeSensorActuator.isChangeAngle())
			changeSensorActuator.setOpeningAngle(openingAngle);
		
		if(changeSensorActuator.isChangeOrientation())
			changeSensorActuator.setOrientation(orientation);
	}

}
