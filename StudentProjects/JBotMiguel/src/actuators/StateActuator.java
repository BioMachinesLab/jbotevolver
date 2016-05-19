package actuators;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class StateActuator extends Actuator{
	
	private int numberOfStates;
	private double[] states;
	
	public StateActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		numberOfStates = args.getArgumentAsIntOrSetDefault("states", 1);
		states = new double[numberOfStates];
	}

	@Override
	public void apply(Robot robot,double timeDelta) {}

	public void setState(int index, double value) {
		states[index] = value;
	}
	
	public double[] getStates() {
		return states;
	}
	
	public double getState(int index) {
		return states[index];
	}

	public int getNumberOfStates() {
		return states.length;
	}
}