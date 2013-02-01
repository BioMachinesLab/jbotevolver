package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowWallButtonChecker extends AllowedObjectsChecker {
	
	private int id;

	public AllowWallButtonChecker(int id) {
		this.id = id;
	}
	
	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.WALLBUTTON;
	}

}
