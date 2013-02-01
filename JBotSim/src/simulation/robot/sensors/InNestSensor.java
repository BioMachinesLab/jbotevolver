package simulation.robot.sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.checkers.AllowNestChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class InNestSensor extends Sensor {

	private ClosePhysicalObjects closeObjects;

	public InNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);

		this.closeObjects = new ClosePhysicalObjects(simulator,
				0,//((NestEnvironment) simulator.getEnvironment()).getNestRadius(),
				new AllowNestChecker());
	}

	public boolean isInNest() {
		CloseObjectIterator nestIterator = closeObjects.iterator();

		while (nestIterator.hasNext()) {
			PhysicalObject nest = nestIterator.next().getObject();
			if (nest.getPosition().distanceTo(robot.getPosition()) < nest
					.getRadius())
				return true;
		}
		return false;
		// return robot.getPosition().length() < ((NestEnvironment)
		// simulator.getEnvironment()).getNestRadius();
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		if(closeObjects != null)
			closeObjects.update(time, teleported);
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		if (isInNest())
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return "isInNest [" + isInNest() + "]";
	}
}