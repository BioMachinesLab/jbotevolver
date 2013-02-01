package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowOrderedRobotsChecker extends AllowedObjectsChecker {
	private int id;

	public AllowOrderedRobotsChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return 	physicalObject.getId() > id && 
				physicalObject.getType() == PhysicalObjectType.ROBOT;
	}
	
	
}
