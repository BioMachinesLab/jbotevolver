package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.util.Arguments;

public class RobotRGBColorNNOutput implements NNOutput {
	boolean controlRed   = true;
	boolean controlGreen = true;
	boolean controlBlue  = true;
	
	double  red   = 0.0f;
	double  green = 0.0f;
	double  blue  = 0.0f;
	
	RobotRGBColorActuator robotRGBColorActuator;
	
	public RobotRGBColorNNOutput(Actuator robotRGBColorActuator, Arguments args) {
		
		this.robotRGBColorActuator = (RobotRGBColorActuator)robotRGBColorActuator;

		if (args.getArgumentIsDefined("mode")) {
			String modeStr = args.getArgumentAsString("mode");

			if (modeStr.contains("r") || modeStr.contains("R"))
				controlRed = true;

			if (modeStr.contains("g") || modeStr.contains("G"))
				controlGreen = true;

			if (modeStr.contains("b") || modeStr.contains("B"))
				controlBlue = true;

			if (!controlRed && !controlGreen && !controlBlue) {
				throw new RuntimeException(
						"RobotRGBColorNNOutput specified, but no correct colors are listed in the mode=... part (mode='"
								+ modeStr + "')");
			}
		} else {
			controlRed = true;
			controlGreen = true;
			controlBlue = true;
		}
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
