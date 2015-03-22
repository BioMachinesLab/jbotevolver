package outputs;

import actuator.ChangeIndividualSensorActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class ChangeIndividualSensorNNOutput extends NNOutput {
	
	private ChangeIndividualSensorActuator changeIndividualSensorActuator ;
	private double range = 0;
	private double openingAngle = 0;
	private double orientation = 0;

	public ChangeIndividualSensorNNOutput(Actuator actuator, Arguments args) {
		super(actuator, args);
		changeIndividualSensorActuator = (ChangeIndividualSensorActuator)actuator;
	}

	@Override
	public int getNumberOfOutputValues() {
		return changeIndividualSensorActuator.getNumberOfOutputs();
	}

	@Override
	public void setValue(int index, double value) {
		if(index == 0 && changeIndividualSensorActuator.isChangeRange())
			range = value;
		else if(index <= 1 && changeIndividualSensorActuator.isChangeAngle())
			openingAngle = value;
		else
			orientation = value;
	}

	@Override
	public void apply() {
		if(changeIndividualSensorActuator.isChangeRange())
			changeIndividualSensorActuator.setRange(range);
		
		if(changeIndividualSensorActuator.isChangeAngle())
			changeIndividualSensorActuator.setOpeningAngle(openingAngle);
		
		if(changeIndividualSensorActuator.isChangeOrientation())
			changeIndividualSensorActuator.setOrientation(orientation);
	}

}
