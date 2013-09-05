package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowObstacleChecker extends AllowedObjectsChecker {
	
	private int id;

	public AllowObstacleChecker(int id) {
		this.id = id;
	}
	
	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.WALLBUTTON ||
			physicalObject.getType()==PhysicalObjectType.WALL;
	}

}
