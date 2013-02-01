package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
 
public class AllowOrderedPreyChecker extends AllowedObjectsChecker {

	private int id;

	public AllowOrderedPreyChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return  physicalObject.getId() > id  &&
				physicalObject.getType() == PhysicalObjectType.PREY && 
				((Prey)physicalObject).isEnabled();
	}

}
