package behaviors;

import epuck.EpuckIRSensor;
import behaviors.Behavior;
import simulation.Simulator;
import simulation.robot.*;
import simulation.util.Arguments;

public class HugLeftWallBehavior extends Behavior {

	public HugLeftWallBehavior(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {
		
		EpuckIRSensor ir = (EpuckIRSensor)robot.getSensorByType(EpuckIRSensor.class);
		DifferentialDriveRobot r = (DifferentialDriveRobot) robot;
		
		if(ir.getSensorReading(3) <= 0.3 && ir.getSensorReading(2) == 0) {
			r.setWheelSpeed(0.1, 0.1);
		} else if(ir.getSensorReading(3) > 0.3) {
			r.setWheelSpeed(0.05, -0.05);
		} else if(ir.getSensorReading(2) < 0.3 && ir.getSensorReading(2) > 0) {
			r.setWheelSpeed(0.1,0.095);
		} else if(ir.getSensorReading(2) > 0.3) {
			r.setWheelSpeed(0.095,0.1);
		}
	}
}
