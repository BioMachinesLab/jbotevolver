package robots;

import java.util.Random;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpingSumo extends JumpingRobot {
	private static final double STDEV_ANGLETOJUMP = 3.19;
	private static final double AVARAGE_ANGLETOJUMP = 59.28;
	private static final double STDEV_OF_DRIVINGAFTERJUMP = 0.07;
	private static final double AVARAGE_OF_DRIVINGAFTERJUMP = 0.16;	
	private boolean statusOfDrivingAfterJumping;
	private double distanceToDrive;
	private double time_startedDriving;
	private double distanceCovaraged;
		
	public JumpingSumo(Simulator simulator, Arguments args) {
		super(simulator, args);
		random = simulator.getRandom();
		timeScale=0.18*(0.1/simulator.getTimeDelta());   //=0.18* 4 because if 1 second takes 40 timesteps in the simulator, timeDelta=0.025 (the twoWheels needed to be 1.90 speed to model JumpingSumo speed and thus it was necessary to slow down the simulator, because with that speed the robot was going through the obstacles)
		delay_to_start_Jumping = 10.3*(0.1/simulator.getTimeDelta());  // =10.3* 4 because if 1 second takes 40 timesteps in the simulator, timeDelta=0.025 ("")		
		distanceBetweenWheels = 0.185;
		wheelDiameter      = 0.10;
	}
	
	
	public void jump() {
		if (!statusOfJumping) {	
			this.angle = (AVARAGE_ANGLETOJUMP + STDEV_ANGLETOJUMP* random.nextGaussian()); //85 ± 4cm (distance) /~90(high)			
			this.power=5;
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
				inicialPosition = new Vector2d(position.getX(),position.getY());
			}
			if (jumping) {
				robotJumping(time, timeDelta);
			}
			if (statusOfDrivingAfterJumping) { 		
				robotDrivingAfterJumping(time,timeDelta);
			}
		} else {
			robotDriving(time, timeDelta);
		}
		for (Actuator actuator : actuators) {
			actuator.apply(this,timeDelta);
		}
	}
	

	@Override
	protected void landing() {
		jumping = false;
		statusOfDrivingAfterJumping = true;
		Vector2d finalPosition=new Vector2d(position.getX(),
				position.getY());
		distanceCovaraged=inicialPosition.distanceTo(finalPosition);  // Sysout +- 0.85m		
		inicialPosition = new Vector2d(position.getX(),
					position.getY());
		distanceToDrive = (AVARAGE_OF_DRIVINGAFTERJUMP + STDEV_OF_DRIVINGAFTERJUMP* random.nextGaussian());				
		time_startedDriving=timeLanding;
		//System.out.println("distances covaraged_whilejUmping"+distanceCovaraged);
		//System.out.println("how much time since the beggining inicialPosition"+ inicialPosition +"posFinal"+ position +" distance" + inicialPosition.distanceTo(position) + "time since startedJumping"+(timeLanding-time_startedJump));

	}
	
	
	protected void robotDrivingAfterJumping(Double time, double timeDelta){
		if (inicialPosition.distanceTo(position) < distanceToDrive ) {
			position.set(position.getX() + timeDelta * (1) / 2.0
					* FastMath.cosQuick(orientation),
					position.getY() + timeDelta * (1) / 2.0
							* FastMath.sinQuick(orientation)); 
		
		} else {
			Vector2d finalPosition=new Vector2d(position.getX(), position.getY());
			distanceCovaraged+=inicialPosition.distanceTo(finalPosition); //Sysout +-1.01m			  (+-20)		
			statusOfDrivingAfterJumping = false;
			statusOfJumping = false;
			hasNotStartedJumping=true;
			//System.out.println("distances covaraged_lansing"+distanceCovaraged);
			//System.out.println("End-how much time "+(time-inicialTime)+  "time JummpingAndDriving "+(time-time_startedJump) +" timeOfDrivingAfterJump " + (time-time_startedDriving) );
		}
	}
	
	@Override
	public boolean ignoreWallCollisions() {
		return statusOfJumping && !statusOfDrivingAfterJumping;
	}

	
	public boolean isDrivingAfterJumping(){
		return statusOfDrivingAfterJumping;
	}
	
	
	public void ignoreStatusOfDrivingAfterJumping(){
		if(statusOfDrivingAfterJumping){
			statusOfDrivingAfterJumping=false;
		 	statusOfJumping = false;
		 	hasNotStartedJumping=true;
		}
	}
	
}
