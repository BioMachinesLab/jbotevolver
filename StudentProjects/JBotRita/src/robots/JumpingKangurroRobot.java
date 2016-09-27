package robots;

import actuator.JumpActuator;
import actuator.JumpActuator.ActuatorStatus;
import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpingKangurroRobot extends JumpingRobot {
	private boolean jumping=false;
	private double time_startedJump;
	 private boolean startJumping=false;
	private boolean isToStop=false;
	private double time_afterjumping=0.0;
	private double angle;
	private boolean isAllowedToJump=true;
	private double z;
	private Vector2d inicialPosition;
	private Vector2d finalPosition;
	private boolean isAscending;
	private double power;
	
	
	public JumpingKangurroRobot(Simulator simulator, Arguments args) {
		super(simulator, args);
		// TODO Auto-generated constructor stub
	}
		
	@Override
	public void updateActuators(Double time, double timeDelta) {
		if(!isAllowedToJump &&  time-time_afterjumping>=0){
			isAllowedToJump=true;
		}
		if(isAllowedToJump && startJumping){
			time_startedJump=time;
			startJumping=false;
			jumping=true;
			inicialPosition=new Vector2d(position.getX(),position.getY());
//			System.out.println("vou começar a saltar"+ angle +"power"+power);
		}
		
		 if(jumping){
//			 angle=50;
			 double power = 3.8;
			 double t=(time-time_startedJump)*timeDelta;
			 double Vo= power*FastMath.cosQuick(Math.toRadians(angle));
			 double Vy=power*FastMath.sinQuick(Math.toRadians(angle));
			 double zAnterior=z;
			 
			 position.set(
						position.getX() + timeDelta * (Vo)/2.7  * FastMath.cosQuick(orientation), //a roda da esquerda e da direita vao mudando,logo aqui nisto de acelar é por uma fixe tipo 0.5... no rightweelspeed e no leftspeed...
						position.getY() + timeDelta * (Vo)/2.7  * FastMath.sinQuick(orientation));

			 z=Vy*t-(9.8/2)*(t*t);
			 if(zAnterior<z){
				 isAscending=true;
//				 System.out.println("tou a crescer a saltar"+ z);
			 }
			 else{
//				 System.out.println("tou a cair"+ z);
				 isAscending=false;
			 }
			 if(z<0){
//					 System.out.println("Tempo do salto em segundos: "+(time-time_startedJump)/10.0);
					 isToStop=true;
					 time_afterjumping=time;
					 isAllowedToJump=false;
//				 System.out.println("inicialPosition"+ inicialPosition +"posFinal"+ position +" distance" + inicialPosition.distanceTo(position));
				 }
		 } else{ 
						 position.set(
						position.getX() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * FastMath.cosQuick(orientation),
						position.getY() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * FastMath.sinQuick(orientation));
					orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * (rightWheelSpeed - leftWheelSpeed); 
					orientation = MathUtils.modPI2(orientation);
			 }
		
		 
		
		for(Actuator actuator: actuators){
			actuator.apply(this);
		}
	}
	 
	public void jump(double angle, double power){
		if(isToStop){
			jumping=false;
			isToStop=false;
		}	
		if(jumping==false){ 
			this.angle=angle;
			this.power=power;
			startJumping=true;
			
		}
	}
	
	public void stopjumping(){
		if(isToStop){
			jumping=false;
			isToStop=false;
		}
	}
	
	@Override
	public boolean ignoreWallCollisions() {
		return jumping;
	}
	
	@Override
	public boolean isJumping(){
		return jumping;
	}
	
	
	@Override
	public boolean canJump(){
		return isAllowedToJump;
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
	
	public double getZ(){
		return z;
	}
	
}



//public class JumpingRobot extends DifferentialDriveRobot {
//	
//	public JumpingRobot(Simulator simulator, Arguments args) {
//		super(simulator, args);
//		// TODO Auto-generated constructor stub
//	}
//	
//	public int numberOfJumpsSucess;
//	public int numberOfJumps;
//	public boolean jump;
//	double time_startedJump;
//	boolean startJumping;
//	
//	@Override
//	public void updateActuators(Double time, double timeDelta) {
//		System.out.println(time+" este é o tempo");
//		if(startJumping){
//			time_startedJump=time;
//			startJumping=false;
//		}
//		
//		//se startedJumping, guardar time
//		//e passar o startedJumping a false
//		
//		 if(jump ){
//			 
//			 if(time-time_startedJump>=20.0)//se ja passaram 20 timesteps desde o startJump
//			 //entao vamos parar de saltar
//				 
//			
//			 position.set(
//						position.getX() + timeDelta * (0.3 + 0.3) / 2.0 * FastMath.cosQuick(orientation), //a roda da esquerda e da direita vao mudando,logo aqui nisto de acelar é por uma fixe tipo 0.5... no rightweelspeed e no leftspeed...
//						position.getY() + timeDelta * (0.3 + 0.3) / 2.0 * FastMath.sinQuick(orientation));
//						
//				orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * ( 0.3 - 0.3); 
//				
//				orientation = MathUtils.modPI2(orientation);
//				
//				numberOfJumps++;
//				
//		 }
//		 else{
//			
//			position.set(
//					position.getX() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * FastMath.cosQuick(orientation),
//					position.getY() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * FastMath.sinQuick(orientation));
//
//				orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * (rightWheelSpeed - leftWheelSpeed); 
//	
//				orientation = MathUtils.modPI2(orientation);
//		}
//		
//		for(Actuator actuator: actuators){
//			actuator.apply(this);
//		}	
//	}
//	
//	
//	public int getNumberOfJumps(){
//		return numberOfJumps;
//	}
//	
//	public int getNumberOfJumpsSucess(){
//		return numberOfJumpsSucess;
//	}
//	
//	public void jump(){
//		if(jump==false){ //se ele antes n tava a saltar,começar a saltar...
//			startJumping=true;
//		}
//		jump=true;
//	}
//	
//	public void stopjumping(){
//		jump=false;
//	}
//	
//	@Override
//	public boolean ignoreWallCollisions() {
//		return jump;
//	}
//	
//}
