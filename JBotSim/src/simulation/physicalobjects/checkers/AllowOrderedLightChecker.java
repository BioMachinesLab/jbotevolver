package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowOrderedLightChecker extends AllowedObjectsChecker {
	
	private int id;

	public AllowOrderedLightChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return  physicalObject.getId() > id  &&
				physicalObject.getType() == PhysicalObjectType.LIGHTPOLE;
	}


}
