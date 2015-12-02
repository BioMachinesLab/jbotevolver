package simulation.robot.actuators;

import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.Factory;

public abstract class Actuator extends SimulatorObject {
	protected int id;
	
	public Actuator(Simulator simulator, int id, Arguments args) {
		super();
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public abstract void apply(Robot robot, double timeDelta);
	
	public static Actuator getActuator(Simulator simulator, String name, Arguments arguments) {
		return (Actuator)Factory.getInstance(name, simulator,arguments.getArgumentAsIntOrSetDefault("id",0),arguments);
	}	
}