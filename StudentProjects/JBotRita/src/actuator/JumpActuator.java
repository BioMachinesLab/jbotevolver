package actuator;

import java.util.Random;

import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpActuator extends Actuator{
	
	public enum ActuatorStatus {
		OFF, JUMP
	}
	
	protected ActuatorStatus status = ActuatorStatus.OFF;
	private Random random;
	
	public JumpActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.random = simulator.getRandom();		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void apply(Robot robot) {
		// TODO Auto-generated method stub
		if(status==ActuatorStatus.JUMP){
			jump(robot);
		}
		else{
			stop(robot);
		}
	}

	public ActuatorStatus getStatus() {
		return status;
	}
	
	public void jumpStatus() {
		status = ActuatorStatus.JUMP;
	}
	
	public void offStatus() {
		status = ActuatorStatus.OFF;
	}
	
	
	public void jump(Robot robot) {
		((JumpingRobot) robot).jump(80,4);
	}

	
	public void stop(Robot robot) {
		((JumpingRobot) robot).stopjumping();
	}
	
}
