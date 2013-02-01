package simulation.robot.behaviors;

import simulation.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;

public abstract class Behavior extends Controller{
	
	protected int numberOfOutputs = 1;
	protected Robot robot;
	protected boolean lock;
	
	public Behavior(Simulator simulator, Robot r, boolean lock) {
		super(simulator, r);
		this.robot = r;
		this.lock = lock;
	}

	public abstract boolean isLocked();
	
	public abstract void applyBehavior();

	public void setValue(int index, double value) {}
	
	public int getNumberOfOutputs() {return numberOfOutputs;}

}