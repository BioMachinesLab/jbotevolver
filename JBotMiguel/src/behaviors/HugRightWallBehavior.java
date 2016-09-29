package behaviors;

import epuck.EpuckIRSensor;
import behaviors.Behavior;
import simulation.Simulator;
import simulation.robot.*;
import simulation.util.Arguments;

public class HugRightWallBehavior extends Behavior {

	public HugRightWallBehavior(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {
		
		EpuckIRSensor ir = (EpuckIRSensor)robot.getSensorByType(EpuckIRSensor.class);
		DifferentialDriveRobot r = (DifferentialDriveRobot) robot;
		
		if(ir.getSensorReading(0) <= 0.3 && ir.getSensorReading(1) == 0) {
			r.setWheelSpeed(0.1, 0.1);
		} else if(ir.getSensorReading(0) > 0.3) {
			r.setWheelSpeed(-0.05, 0.05);
		} else if(ir.getSensorReading(1) < 0.3 && ir.getSensorReading(1) > 0) {
			r.setWheelSpeed(0.095,0.1);
		} else if(ir.getSensorReading(1) > 0.3) {
			r.setWheelSpeed(0.1,0.095);
		}
	}
}
