package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.target.Target;
import commoninterface.utils.CIArguments;

public class ConeTargetCISensor extends WaypointConeCISensor {
	private static final long serialVersionUID = -5789316736781553940L;

	public ConeTargetCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);
	}

	@Override
	public boolean validEntity(Entity e) {
		return e instanceof Target;
	}
}
