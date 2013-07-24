package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.util.Arguments;

public class RobotRGBColorNNOutput extends NNOutput {
	boolean controlRed   = true;
	boolean controlGreen = true;
	boolean controlBlue  = true;
	
	double  red   = 0.0f;
	double  green = 0.0f;
	double  blue  = 0.0f;
	
	private RobotRGBColorActuator robotRGBColorActuator;
	
	public RobotRGBColorNNOutput(Actuator robotRGBColorActuator, Arguments args) {
		super(robotRGBColorActuator,args);
		this.robotRGBColorActuator = (RobotRGBColorActuator)robotRGBColorActuator;
		controlRed = this.robotRGBColorActuator.controlRed();
		controlGreen = this.robotRGBColorActuator.controlGreen();
		controlBlue = this.robotRGBColorActuator.controlBlue();
	}
	
	public void setRed(double value) {
		this.red = value;
	}
	
	public void setGreen(double value) {
		this.green = value;
	}
	
	public void setBlue(double value) {
		this.blue = value;
	}
	
	@Override
	public void apply() {
		robotRGBColorActuator.setAll(red, green, blue);
	}

	@Override
	public int getNumberOfOutputValues() {
		int outputs = 
			(controlRed   ? 1 : 0) +
			(controlGreen ? 1 : 0) +
			(controlBlue  ? 1 : 0);
		
		return outputs;
	}

	@Override
	public void setValue(int index, double value) {
		if (index == 0) {
			if (controlRed)
				red = value;
			else if (controlGreen) 
				green = value;
			else 
				blue = value;
		} else if (index == 1) {
			if (controlRed) {
				if (controlGreen)
					green = value;
				else 
					blue = value;
			}
		} else {
			blue = value;
		}
	}
}