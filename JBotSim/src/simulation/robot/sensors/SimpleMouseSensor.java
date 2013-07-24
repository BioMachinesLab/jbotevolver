package simulation.robot.sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleMouseSensor extends SimpleLightTypeSensor {

	public SimpleMouseSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowMouseChecker());
		
		initAngles();
	}

	private void initAngles() {
		angles[1] = -Math.PI/6;
		angles[0] = Math.PI/6;
	}
	
}

