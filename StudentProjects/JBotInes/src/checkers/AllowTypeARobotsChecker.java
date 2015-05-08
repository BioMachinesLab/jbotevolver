package checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;

public class AllowTypeARobotsChecker extends AllowedObjectsChecker {
	private int id;

	public AllowTypeARobotsChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		if(physicalObject.getId()!=id && physicalObject.getType()==PhysicalObjectType.ROBOT) {
			Robot r = (Robot)physicalObject;
			return (!r.getDescription().equals("prey") && r.getDescription().equals("type0"));
		}
		return false;
	}
}
