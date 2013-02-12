package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.util.Arguments;

public class RobotColorNNOutput extends NNOutput {

	private RobotColorActuator robotColor;
	
	public RobotColorNNOutput(Actuator robotColor, Arguments args) {
		super(robotColor,args);
		this.robotColor = (RobotColorActuator)robotColor;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if (value < 0.33) {
			robotColor.turnBlack();
		} else if (value < 0.66) {
			robotColor.turnGreen();			
		} else {
			robotColor.turnRed();			
		}
	}

	@Override
	public void apply() {}
}