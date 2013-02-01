package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowWallRobotChecker extends AllowedObjectsChecker{
	
	private int id;

	public AllowWallRobotChecker(int id) {
		this.id = id;
	}

	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.WALL ||
			physicalObject.getType()==PhysicalObjectType.WALLBUTTON ||
			(physicalObject.getType()==PhysicalObjectType.ROBOT && physicalObject.getId()!=id);
	}
	
}
