package fourwheeledrobot;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class MultipleWheelRepertoireNDNNOutput extends NNOutput {
	
	private MultipleWheelRepertoireNDActuator actuator;
	private double[] actuations;
	
	public MultipleWheelRepertoireNDNNOutput(Actuator actuator, Arguments args) {
		super(actuator,args);
		this.actuator  = (MultipleWheelRepertoireNDActuator)actuator;
		actuations = new double[this.actuator.getNDimensions()];
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return actuator.getNDimensions();
	}

	@Override
	public void setValue(int output, double value) {
		actuations[output] = value;
	}
	
	@Override
	public void apply() {
		for(int i = 0 ; i < actuations.length ; i++)
		actuator.setActuation(actuations[i], i);
	}
}