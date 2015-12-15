package fourwheeledrobot;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AWS_6Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * axle speed + independent wheel steering
	 */
	public AWS_6Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,2,4);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speeds = new double[]{this.speeds[0],this.speeds[0],this.speeds[1],this.speeds[1]};
		double[] rotations = this.rotation;
		this.actuateRobot(robot, timeDelta, speeds, rotations);
	}
}