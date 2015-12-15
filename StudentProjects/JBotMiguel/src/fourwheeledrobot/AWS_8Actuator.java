package fourwheeledrobot;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AWS_8Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * independent wheel speed + independent wheel steering
	 */
	public AWS_8Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,4,4);
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[1],this.speeds[2],this.speeds[3]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[1],this.rotation[2],this.rotation[3]};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
	
}