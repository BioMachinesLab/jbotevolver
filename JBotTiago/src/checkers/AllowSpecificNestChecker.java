package checkers;

import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;

public class AllowSpecificNestChecker extends AllowedObjectsChecker {
	
	String specificNest;
	
	public AllowSpecificNestChecker(String specificNest)  {
		this.specificNest = specificNest; 
	}
	
	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		
		if(physicalObject.getType()==PhysicalObjectType.NEST) {
			Nest n = (Nest)physicalObject;
			return n.getName().equalsIgnoreCase("Nest" + specificNest.toUpperCase());
		}
		return false;
	}

}
