package simulation.physicalobjects.checkers;

import java.util.LinkedList;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowArbitraryChecker extends AllowedObjectsChecker{
	
	private LinkedList<PhysicalObjectType> types = new LinkedList<PhysicalObjectType>();
	
	public AllowArbitraryChecker() {}
	
	public void add(PhysicalObjectType p) {
		types.add(p);
	}
	
	public boolean isAllowed(PhysicalObject physicalObject) {
		for(PhysicalObjectType p : types)
			if(physicalObject.getType()==p)
				return true;
		return false;
	}

}
