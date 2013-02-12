package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.util.Arguments;

public class MultiNeuronRobotColorNNOutput extends NNOutput {
	protected int 	   numberOfColors;
	protected double[] activations;
	protected RobotColorActuator robotColorActuator;
	
	public MultiNeuronRobotColorNNOutput(Actuator robotColorActuator, Arguments args) {
		super(robotColorActuator,args);
		numberOfColors = args.getArgumentIsDefined("numberofcolors") ? args.getArgumentAsInt("numberofcolors") : 4;
		this.activations = new double[numberOfColors];
		this.robotColorActuator = (RobotColorActuator)robotColorActuator;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return numberOfColors;
	}

	@Override
	public void setValue(int index, double value) {
		activations[index] = value;
	}

	@Override
	public void apply() {
		int  	highestIndex 		= 0;
		double 	highestActivation	= activations[0];
		
		for (int i = 1; i < activations.length; i++) {
			if (activations[i] > highestActivation) {
				highestActivation = activations[i];
				highestIndex      = i;
			}
		}

		switch (highestIndex) {
		case 0: robotColorActuator.turnRed();       break;
		case 1: robotColorActuator.turnGreen();     break;
		case 2: robotColorActuator.turnBlue();      break;
		case 3: robotColorActuator.turnLightGrey(); break;
		case 4: robotColorActuator.turnDarkGrey();  break;
		case 5: robotColorActuator.turnYellow();    break;
		} 
	}
}