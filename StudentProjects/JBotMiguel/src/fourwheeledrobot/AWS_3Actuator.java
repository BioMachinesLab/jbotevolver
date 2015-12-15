package fourwheeledrobot;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AWS_3Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * single accel + independent axle steering
	 */
	
	public AWS_3Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,1,2);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[0],this.speeds[0],this.speeds[0]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[0],this.rotation[1],this.rotation[1]};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
	
}
