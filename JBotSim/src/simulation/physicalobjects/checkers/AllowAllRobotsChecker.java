package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;

public class AllowAllRobotsChecker extends AllowedObjectsChecker {
	private int id;

	public AllowAllRobotsChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		if(physicalObject.getId()!=id && physicalObject.getType()==PhysicalObjectType.ROBOT) {
			Robot r = (Robot)physicalObject;
			return !r.getDescription().equals("prey");
		}
		return false;
	}
	
}
