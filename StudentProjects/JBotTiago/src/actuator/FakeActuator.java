package actuator;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class FakeActuator extends Actuator {
	
	private double value;

	public FakeActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public void apply(Robot robot) {
		if(value > 0.5){
			if(robot instanceof DifferentialDriveRobot) {
				DifferentialDriveRobot ddr = (DifferentialDriveRobot)robot;
				ddr.setWheelSpeed(0,0);
			}
		}
	}	

}
