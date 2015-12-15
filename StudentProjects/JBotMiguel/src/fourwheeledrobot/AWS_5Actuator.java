package fourwheeledrobot;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AWS_5Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * single speed + independent wheel steering
	 */
	public AWS_5Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,1,4);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[0],this.speeds[0],this.speeds[0]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[1],this.rotation[2],this.rotation[3]};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
}