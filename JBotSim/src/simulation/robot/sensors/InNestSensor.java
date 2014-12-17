package simulation.robot.sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.checkers.AllowNestChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class InNestSensor extends Sensor {

	private ClosePhysicalObjects closeObjects;
	private Simulator simulator;

	public InNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
	}

	public boolean isInNest() {
		
		if(closeObjects == null) {
			//first call
			double radius = 0;
			for(PhysicalObject p : simulator.getEnvironment().getAllObjects()) {
				if(p instanceof Nest)
					radius = Math.max(radius, p.getRadius());
			}
			
			this.closeObjects = new ClosePhysicalObjects(simulator.getEnvironment(),
					radius,
					new AllowNestChecker());
		}
		
		CloseObjectIterator nestIterator = closeObjects.iterator();

		while (nestIterator.hasNext()) {
			PhysicalObject nest = nestIterator.next().getObject();
			if (nest.getPosition().distanceTo(robot.getPosition()) < nest
					.getRadius())
				return true;
		}
		return false;
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