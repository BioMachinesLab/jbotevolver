package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

public class TwoWheelNNOutput implements NNOutput {
	private double maxSpeed;
	private TwoWheelActuator twoWheelActuator;
	private float noiseOffset;
	
	public TwoWheelNNOutput(Actuator twoWheelActuator, Arguments arguments) {
		this.twoWheelActuator  = (TwoWheelActuator)twoWheelActuator;
		this.maxSpeed          = arguments.getArgumentIsDefined("maxspeed") ? arguments.getArgumentAsDouble("maxspeed") : Robot.MAXIMUMSPEED;
		this.noiseOffset	   = arguments.getArgumentIsDefined("noiseoffset") ? (float)arguments.getArgumentAsDouble("noiseoffset") : 0;
//		this.maxSpeed         += maxSpeed*noiseOffset*sim.getRandom().nextGaussian();
	}
	
//	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

//	@Override
	public void setValue(int output, double value) {
		if (output == 0)
			twoWheelActuator.setLeftWheelSpeed(value); 
		else
			twoWheelActuator.setRightWheelSpeed(value);
	}

	@Override
	public void apply() {
	}
}
