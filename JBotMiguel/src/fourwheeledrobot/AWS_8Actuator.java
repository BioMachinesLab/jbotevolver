package fourwheeledrobot;

import fourwheeledrobot.MultipleWheelAxesActuator;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AWS_8Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * Normal car with RWD
	 */
	public AWS_8Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,4,4);
	}
	
	@Override
	public double[] getCompleteRotations() {
		return new double[]{this.rotation[0],this.rotation[1],this.rotation[2],this.rotation[3]};
	}
	
	@Override
	public double[] getCompleteSpeeds() {
		return new double[]{this.speeds[0],this.speeds[1],this.speeds[2],this.speeds[3]};
	}
	
}
