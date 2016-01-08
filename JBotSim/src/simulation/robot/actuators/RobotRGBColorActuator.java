package simulation.robot.actuators;

import java.awt.Color;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class RobotRGBColorActuator extends Actuator {

	@Override
	public String toString() {
		return "RobotRGBColorActuator [red=" + red + ", green=" + green
				+ ", blue=" + blue + "]";
	}

	protected double red   = 0.0f;
	protected double green = 0.0f;
	protected double blue  = 0.0f;
	
	boolean controlRed   = false;
	boolean controlGreen = false;
	boolean controlBlue  = false;
	
	@ArgumentsAnnotation(name="mode", values={"","R","G","B", "RG", "RB", "GB", "RGB"})
	private String modeStr;
	
	public RobotRGBColorActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		if (args.getArgumentIsDefined("mode")) {
			modeStr = args.getArgumentAsString("mode");
			
			if (modeStr.contains("r") || modeStr.contains("R"))
				controlRed = true;

			if (modeStr.contains("g") || modeStr.contains("G"))
				controlGreen = true;

			if (modeStr.contains("b") || modeStr.contains("B"))
				controlBlue = true;

			if (!controlRed && !controlGreen && !controlBlue) {
				throw new RuntimeException(
						"RobotRGBColorActuator specified, but no correct colors are listed in the mode=... part (mode='"
								+ modeStr + "')");
			}
		} else {
			controlRed = true;
			controlGreen = true;
			controlBlue = true;
		}
	}
	
	public boolean controlRed() {
		return controlRed;
	}
	
	public boolean controlBlue() {
		return controlBlue;
	}
	
	public boolean controlGreen() {
		return controlGreen;
	}

	public void setRed(double red) {
		this.red = red;
	}
	
	public void setGreen(double green) {
		this.green = green;
	}
	
	public void setBlue(double blue) {
		this.blue = blue;
	}
	
	public void setAll(double red, double green, double blue) {
		this.red   = red;
		this.green = green;
		this.blue  = blue;
	}
	
	public void setAll(double[] color) {
		this.red   = color[0];
		this.green = color[1];
		this.blue  = color[2];
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		robot.setBodyColor(red, green, blue);
	}
}