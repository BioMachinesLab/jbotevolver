package fourwheeledrobot;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AWS_4Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * axle accel + independent axle steering
	 */
	public AWS_4Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,2,2);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[0],this.speeds[1],this.speeds[1]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[0],this.rotation[1],this.rotation[1]};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
}