package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;

public class AllowMouseChecker extends AllowedObjectsChecker {

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		
		if(physicalObject.getType()==PhysicalObjectType.ROBOT) {
			Robot r = (Robot)physicalObject;
			return r.getDescription().equals("prey");
		}
		return false;
	}
	
}
