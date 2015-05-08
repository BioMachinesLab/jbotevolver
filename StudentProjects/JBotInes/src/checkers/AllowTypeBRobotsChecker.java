package checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;

public class AllowTypeBRobotsChecker extends AllowedObjectsChecker {
	private int id;

	public AllowTypeBRobotsChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		if(physicalObject.getId()!=id && physicalObject.getType()==PhysicalObjectType.ROBOT) {
			Robot r = (Robot)physicalObject;
			return (!r.getDescription().equals("prey") && r.getDescription().equals("type1"));
		}
		return false;
	}
}
