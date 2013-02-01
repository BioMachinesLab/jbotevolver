package simulation.robot.actuators;

import simulation.Simulator;
import simulation.robot.Robot;

public class RobotRGBColorActuator extends Actuator {

	@Override
	public String toString() {
		return "RobotRGBColorActuator [red=" + red + ", green=" + green
				+ ", blue=" + blue + "]";
	}

	protected double red   = 0.0f;
	protected double green = 0.0f;
	protected double blue  = 0.0f;
	
	public RobotRGBColorActuator(Simulator simulator, int id) {
		super(simulator, id);
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
	public void apply(Robot robot) {
		robot.setBodyColor(red, green, blue);
	}
}