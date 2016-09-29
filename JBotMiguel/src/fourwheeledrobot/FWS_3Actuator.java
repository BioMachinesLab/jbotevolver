package fourwheeledrobot;

import fourwheeledrobot.MultipleWheelAxesActuator;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FWS_3Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * Normal car with RWD
	 */
	public FWS_3Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,1,2);
	}
	
	@Override
	public double[] getCompleteRotations() {
		return new double[]{this.rotation[0],this.rotation[1],0,0};
	}
	
	@Override
	public double[] getCompleteSpeeds() {
		return new double[]{this.speeds[0],this.speeds[0],this.speeds[0],this.speeds[0]};
	}
	
}
