package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowWallChecker extends AllowedObjectsChecker{

	private static final long serialVersionUID = 8627838930684025813L;

	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.WALL ||
		physicalObject.getType()==PhysicalObjectType.WALLBUTTON;
	}
}
