package actuator;

import java.util.Random;

import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpParameterActuator extends Actuator{
	
	public enum ActuatorStatus {
		OFF, JUMP
	}
	
	private double angle;
	private double power;
	private ActuatorStatus status = ActuatorStatus.OFF;
	@ArgumentsAnnotation(name="maxpower", defaultValue = "4")
	protected double maxPower;
	
	
	public JumpParameterActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.maxPower = args.getArgumentAsDoubleOrSetDefault("maxpower", 4);
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
		((JumpingRobot) robot).jump(angle, power);
	}

	
	public void stop(Robot robot) {
		((JumpingRobot) robot).stopjumping();
	}
	
	public void setAngle(double angle){
		this.angle=angle*90;
	}
	
	public void setPower(double power){
		this.power=power*maxPower;
	}
}
