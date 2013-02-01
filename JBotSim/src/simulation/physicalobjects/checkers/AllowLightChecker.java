/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package simulation.physicalobjects.checkers;

/**
 *
 * @author Borga
 */

import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;

public class AllowLightChecker extends AllowedObjectsChecker {

    @Override
	public boolean isAllowed(PhysicalObject physicalObject) {
		return physicalObject.getType()==PhysicalObjectType.LIGHTPOLE;
	}

}
