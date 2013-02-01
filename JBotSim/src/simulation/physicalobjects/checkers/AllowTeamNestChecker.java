package simulation.physicalobjects.checkers;

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowTeamNestChecker extends AllowedObjectsChecker {

	private int team;
	
	
	public AllowTeamNestChecker(int team) {
		super();
		this.team = team;
	}


	@Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		 if(physicalObject.getType()==PhysicalObjectType.NEST)
		 	return (physicalObject.getParameterAsInteger("TEAM") == team);
		 return false;
	}

}
