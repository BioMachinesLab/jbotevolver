package simulation.robot.sensors;

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

	public void update(double time, ArrayList<PhysicalObject> teleported) {
	};

	public int getId() {
		return id;
	}

}