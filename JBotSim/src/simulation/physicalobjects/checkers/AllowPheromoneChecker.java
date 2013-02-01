package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowPheromoneChecker extends AllowedObjectsChecker {
	
	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.PHEROMONE;
	}

}
