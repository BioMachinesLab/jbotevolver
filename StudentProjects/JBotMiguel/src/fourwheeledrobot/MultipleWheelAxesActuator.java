package fourwheeledrobot;

import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public abstract class MultipleWheelAxesActuator extends Actuator{
	
	@ArgumentsAnnotation(name="wheeldiameter", defaultValue = "0.05")
	protected double wheelDiameter = 0.05;
	
	@ArgumentsAnnotation(name="distancewheels", defaultValue = "0.05")
	protected double distanceBetweenWheels = 0.05;
	
	@ArgumentsAnnotation(name="maxspeed", defaultValue = "0.1")
	protected double maxSpeed = 0.1;
	
	protected double[] speeds;
	protected double[] rotation;

	public MultipleWheelAxesActuator(Simulator simulator, int id, Arguments args, int wheels, int axes) {
		super(simulator, id, args);
		speeds = new double[wheels];
		rotation = new double[axes];
	}
	
	public void setWheelSpeed(int wheelNumber, double val) {
		speeds[wheelNumber] = ( val - 0.5) * maxSpeed * 2.0;
	}
	
	public void setWheelSpeed(double[] vals) {
		for(int i = 0 ; i < vals.length ; i++) {
			setWheelSpeed(i,vals[i]);
		}
	}
	
	public void setRotation(int axisNumber, double val) {
		rotation[axisNumber] = val*Math.PI*2 - Math.PI;
	}
	
	public void setRotation(double[] vals) {
		for(int i = 0 ; i < vals.length ; i++) {
			setRotation(i,vals[i]);
		}
	}
	
	public double getMaxSpeed() {
		return maxSpeed;
	}
	
	public double[] getSpeed(){
		return speeds;
	}
	
	public double[] getRotation() {
		return rotation;
	}
	
	public int getNumberOfWheels() {
		return speeds.length;
	}
	
	public int getNumberOfAxes() {
		return rotation.length;
	}

}
