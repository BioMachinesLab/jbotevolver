package fourwheeledrobot;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FWS_4Actuator extends MultipleWheelAxesActuator{
	
	/**
	 * axle accel + independent front wheel steering
	 */
	
	public FWS_4Actuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,2,2);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		double[] speed = new double[]{this.speeds[0],this.speeds[0],this.speeds[1],this.speeds[1]};
		double[] rotation = new double[]{this.rotation[0],this.rotation[1],0,0};
		this.actuateRobot(robot, timeDelta, speed, rotation);
	}
	
}
