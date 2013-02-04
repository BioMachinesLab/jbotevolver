package simulation.robot.actuators;

import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

public abstract class Actuator extends SimulatorObject {
	protected int id;
	
	public Actuator(Simulator simulator, int id, Arguments args) {
		super();
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public abstract void apply(Robot robot);
}