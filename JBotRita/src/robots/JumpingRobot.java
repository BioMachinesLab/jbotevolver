package robots;

import java.util.Random;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpingRobot extends DifferentialDriveRobot {

	protected boolean jumping = false;
	protected boolean hasNotStartedJumping = true;
	protected double time_startedJump;
	protected double angle;
	protected boolean isAscending;
	protected double power;
	protected Vector2d inicialPosition;
	protected double z;
	protected Double timeLanding;
	protected boolean isToJump = false;
	protected double inicialTime;
	protected boolean statusOfJumping = false;
	protected Random random;

	@ArgumentsAnnotation(name="delay_to_start_Jumping", defaultValue = "10.0")
	protected double delay_to_start_Jumping;
	
	@ArgumentsAnnotation(name="timeScale", defaultValue = "0.18")
	protected double timeScale;
	
	
	public JumpingRobot(Simulator simulator, Arguments args) {
		super(simulator, args);
		random = simulator.getRandom();
		this.delay_to_start_Jumping = args.getArgumentAsDoubleOrSetDefault("delay_to_start_Jumping", 10.0);
		this.timeScale = args.getArgumentAsDoubleOrSetDefault("timeScale", 0.18);

	}
	
	public void jump(double angle, double power) {
		if (!statusOfJumping) {
			this.angle = angle;
			this.power = power;
			isToJump = true;
		}
	}

	@Override
	public void updateActuators(Double time, double timeDelta) {
		if (isToJump) {
			inicialTime = time;
			isToJump = false;
			statusOfJumping = true;
		}
		if (statusOfJumping) {
			if (hasNotStartedJumping
					&& time - inicialTime >= delay_to_start_Jumping) { //charging
				jumping = true;
				time_startedJump = time;
				hasNotStartedJumping = false;
				inicialPosition = new Vector2d(position.getX(), position.getY());
			}
			if (jumping) {
				robotJumping(time,timeDelta);
			}
		} else { 
			robotDriving(time, timeDelta);
		}
		for (Actuator actuator : actuators) {
			actuator.apply(this, timeDelta);
		}
	}

	protected void robotJumping(Double time, double timeDelta){
		double t = ((time - time_startedJump) * (timeDelta))
				/ timeScale;		
		double Vo = power * FastMath.cosQuick(Math.toRadians(angle));
		double Vy = power * FastMath.sinQuick(Math.toRadians(angle));
		double zAnterior = z;
		
		position.set(position.getX() + (timeDelta / timeScale) * (Vo)
				/ 2.7 * FastMath.cosQuick(orientation),
				position.getY() + (timeDelta / timeScale) * (Vo) / 2.7
						* FastMath.sinQuick(orientation));
		
		z = Vy * t - (9.8 / 2) * (t * t);
		if (zAnterior < z) {
			isAscending = true;
		} else {
			isAscending = false;
		}
		
		if (z < 0) {
			timeLanding=time;
			landing();
		}
	}
	
	protected void robotDriving(Double time, double timeDelta){
		inicialPosition = new Vector2d(position.getX(), position.getY());
		position.set(position.getX() + timeDelta* (leftWheelSpeed + rightWheelSpeed) / 2.0* FastMath.cosQuick(orientation), position.getY()+ timeDelta * (leftWheelSpeed + rightWheelSpeed)/ 2.0 * FastMath.sinQuick(orientation));
		orientation += timeDelta * 0.5 / (distanceBetweenWheels / 2.0)* (rightWheelSpeed - leftWheelSpeed);
		orientation = MathUtils.modPI2(orientation);
		//System.out.println("tou a andar inicialPosition"+ inicialPosition +"posFinal"+ position +" distance" + inicialPosition.distanceTo(position));
	}
	
	protected void landing() {  
		statusOfJumping = false;
		jumping = false;
		hasNotStartedJumping = true;
	}

	public Double getTimeLanding(){
		return timeLanding;
	}
	
	@Override
	public boolean ignoreWallCollisions() {
		return statusOfJumping ;
	}

	public boolean statusOfJumping() {
		return statusOfJumping;
	}


	public boolean isJumping() {
		return jumping;
	}


	public boolean isAscending() {
		return isAscending;
	}

	public double getRightWheelSpeed() {
		return rightWheelSpeed;
	}

	
	public double getLeftWheelSpeed() {
		return leftWheelSpeed;
	}

	public void stopJumping() {  //forWallsCollision
		statusOfJumping = false;
		jumping = false;
		hasNotStartedJumping = true;
	}
	
	public boolean charging(){
		return statusOfJumping==true && hasNotStartedJumping==false;
	}
	
}
