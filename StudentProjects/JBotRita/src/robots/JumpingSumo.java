package robots;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;


public class JumpingSumo extends JumpingRobot {
	private boolean jumping=false;
	private double time_startedJump;
	private double angle;
	private boolean isAllowedToStartJumping=false;
	private double z;
	private boolean isAscending;
	private double power;
	private double delayToStartJumping;
	private static final int HIGHJUMP_DELAY=20;
	private static final int LONGJUMP_DELAY=30;
	private static final int HIGHJUMP_ANGLE=60;
	private boolean isToJump=false;
	private double inicialTime;
	private boolean statusOfJumping=false;
	private boolean jumpingHigh=false;
	
	private Vector2d inicialPosition;
	private Vector2d finalPosition;
	
	public JumpingSumo(Simulator simulator, Arguments args) {
		super(simulator, args);
	}
		
	@Override
	public void updateActuators(Double time, double timeDelta) {
		
		if(isToJump){
			
			inicialTime=time;
			isToJump=false;
			statusOfJumping=true;
			
		}
		
		if(statusOfJumping){
		
			if(!jumping && time-inicialTime>=delayToStartJumping){
				jumping=true;
				time_startedJump=time;
				inicialPosition=new Vector2d(position.getX(), position.getY());
			}
			
			if(jumping){
				//System.out.println("estou a saltar");
				
				double timeScale = 5;
				 double t=((time-time_startedJump)*(timeDelta))/timeScale;
				 double Vo= power*FastMath.cosQuick(Math.toRadians(angle));
				 double Vy=power*FastMath.sinQuick(Math.toRadians(angle));
				 double zAnterior=z;
				 
				 position.set(
							position.getX() + (timeDelta/timeScale) * (Vo)/2.7  * FastMath.cosQuick(orientation), //a roda da esquerda e da direita vao mudando,logo aqui nisto de acelar é por uma fixe tipo 0.5... no rightweelspeed e no leftspeed...
							position.getY() + (timeDelta/timeScale) * (Vo)/2.7  * FastMath.sinQuick(orientation));

				 z=Vy*t-(9.8/2)*(t*t);
				 if(zAnterior<z){
					 isAscending=true;
					// System.out.println("tou a crescer a saltar"+ z);
				 }
				 else{
//					 System.out.println("tou a cair"+ z);
					 isAscending=false;
				 }
				 if(z<0){
//						 	System.out.println("Tempo do salto em segundos: "+(time-time_startedJump)/10.0);
					 	jumping=false;
						statusOfJumping=false;
					//	System.out.println("inicialPosition"+ inicialPosition +"posFinal"+ position +" distance" + inicialPosition.distanceTo(position) + "tempo"+(time-time_startedJump) + "JumpinUp"+jumpingHigh);
					 }
			}	
		}
		 else{
				//inicialPosition=new Vector2d(position.getX(),position.getY());
				position.set(
						position.getX() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * FastMath.cosQuick(orientation),
						position.getY() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * FastMath.sinQuick(orientation));
					orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * (rightWheelSpeed - leftWheelSpeed); 
					orientation = MathUtils.modPI2(orientation);
					
				//	 System.out.println("inicialPosition"+ inicialPosition +"posFinal"+ position +" distance" + inicialPosition.distanceTo(position));
			}
		
		for(Actuator actuator: actuators){
			actuator.apply(this);
			
		}
	}
	 
	public void jump(double angle, double power){
		if(!statusOfJumping){ 
			this.angle=angle;
			this.power=power;
			isToJump=true;
			if(angle==HIGHJUMP_ANGLE){
				delayToStartJumping=HIGHJUMP_DELAY;
				jumpingHigh=true;
				
			}
			else{
				delayToStartJumping=LONGJUMP_DELAY;
				jumpingHigh=false;
			}
		}
	}
	

	
	@Override
	public boolean ignoreWallCollisions() {
		return statusOfJumping;
	}
	
	@Override
	public boolean isJumping(){
		return statusOfJumping;
	}
	
	
	@Override
	public boolean canJump(){
		return isAllowedToStartJumping;
	}

	@Override
	public boolean isAscending(){
		return isAscending;
	}
	
	@Override
	public double getRightWheelSpeed() {
		return  rightWheelSpeed;
	}
	
	@Override
	public double getLeftWheelSpeed() {
		return  leftWheelSpeed;
	}
	
	public boolean isJumpingUp(){
		return jumpingHigh;
	}

}

