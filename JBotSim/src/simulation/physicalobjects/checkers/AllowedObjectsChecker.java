package simulation.physicalobjects.checkers;

import java.io.Serializable;

import simulation.physicalobjects.PhysicalObject;

public class AllowedObjectsChecker implements Serializable {
	
	public boolean isAllowed(PhysicalObject physicalObject) {
		return true;
	}

}
