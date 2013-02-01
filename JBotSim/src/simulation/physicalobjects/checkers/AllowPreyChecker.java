package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;

public class AllowPreyChecker extends AllowedObjectsChecker {

	private int id;

	public AllowPreyChecker(int id) {
		this.id = id;
	}

	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return  physicalObject.getType()==PhysicalObjectType.PREY && 
				physicalObject.getId()!=id  &&
				((Prey)physicalObject).isEnabled();
	}

}
