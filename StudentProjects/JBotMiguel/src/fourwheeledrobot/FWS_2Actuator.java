package fourwheeledrobot;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FWS_2Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * Normal car with RWD
	 */
	public FWS_2Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,1,1);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[0],this.speeds[0],this.speeds[0]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[0],0,0};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
	
}
