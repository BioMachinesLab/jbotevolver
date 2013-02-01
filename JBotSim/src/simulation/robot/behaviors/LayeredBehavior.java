package simulation.robot.behaviors;

import simulation.Simulator;
import simulation.environment.TMazeEnvironment;
import simulation.robot.Robot;

public class LayeredBehavior extends Behavior {

	private TMazeEnvironment env;
	private int currentIndex = 0;
	private static double MAX_SPEED = 0.05;
	private double lastValue = 0;
	private double threshold = 0.02;
	
	public LayeredBehavior(Simulator simulator, Robot r, boolean lock, TMazeEnvironment env) {
		super(simulator, r, lock);
		this.env = env;
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public void applyBehavior() {
		if(currentIndex == 0)
			robot.setWheelSpeed(MAX_SPEED, MAX_SPEED);
	}
	
	@Override
	public void setValue(int index, double value) {
		
		if(Math.abs(value-lastValue) < threshold) {
			if(value >= 0 && value <= 0.2)
				currentIndex = 0;
			if(value > 0.2 && value <= 0.4)
				currentIndex = 1;
			if(value > 0.4 && value <= 0.6)
				currentIndex = 2;
			if(value > 0.6 && value <= 0.8)
				currentIndex = 3;
			if(value > 0.8 && value <= 1)
				currentIndex = 4;
		}
		
		lastValue = value;
	}

}
