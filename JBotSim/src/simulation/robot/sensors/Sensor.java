package simulation.robot.sensors;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

public abstract class Sensor extends SimulatorObject {
	protected int id;
	protected Robot robot;
	protected static final int DEFAULT_RANGE = 1;
	protected static final double DEFAULT_OPENING_ANGLE = Math.PI / 2;

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
		
		try {
			Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 4 && params[0] == Simulator.class && params[1] == int.class
						&& params[2] == Robot.class && params[3] == Arguments.class)
					return (Sensor) constructor.newInstance(simulator,id,robot,arguments);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown sensor: " + name);
	}
}