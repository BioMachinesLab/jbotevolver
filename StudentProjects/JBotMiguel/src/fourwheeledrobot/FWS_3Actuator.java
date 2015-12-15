package fourwheeledrobot;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FWS_3Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * single accel + independent front wheel steering
	 */
	
	public FWS_3Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,1,2);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[0],this.speeds[0],this.speeds[0]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[1],0,0};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
	
}
