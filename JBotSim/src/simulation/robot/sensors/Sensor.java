package simulation.robot.sensors;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.Factory;

public abstract class Sensor extends SimulatorObject {
	protected int id;
	protected Robot robot;
	protected static final int DEFAULT_RANGE = 1;
	protected static final double DEFAULT_OPENING_ANGLE = Math.PI / 2;
	protected boolean enabled = false;

	public Sensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super();
		this.id = id;
		this.robot = robot;
	}

	public abstract double getSensorReading(int sensorNumber);

	public void update(double time, ArrayList<PhysicalObject> teleported) {}

	public int getId() {
		return id;
	}
	
	public static Sensor getSensor(Robot robot, Simulator simulator, String name, Arguments arguments) {
		int id = arguments.getArgumentAsIntOrSetDefault("id",0);
		return (Sensor)Factory.getInstance(name, simulator,id,robot,arguments);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}