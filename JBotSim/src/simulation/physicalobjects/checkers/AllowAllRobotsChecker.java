package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowAllRobotsChecker extends AllowedObjectsChecker {
	private int id;

	public AllowAllRobotsChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return  physicalObject.getId()!=id && 
				physicalObject.getType()==PhysicalObjectType.ROBOT;
	}
	
	
}
