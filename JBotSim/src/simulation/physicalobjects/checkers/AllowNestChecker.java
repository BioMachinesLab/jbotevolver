package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowNestChecker extends AllowedObjectsChecker {

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.NEST;
	}

}
