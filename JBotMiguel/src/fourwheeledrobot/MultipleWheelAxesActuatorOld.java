package fourwheeledrobot;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public abstract class MultipleWheelAxesActuatorOld extends Actuator{
	
	@ArgumentsAnnotation(name="wheeldiameter", defaultValue = "0.05")
	protected double wheelDiameter = 0.05;
	
	@ArgumentsAnnotation(name="distancewheels", defaultValue = "0.05")
	protected double distanceBetweenWheels = 0.05;
	
	@ArgumentsAnnotation(name="maxspeed", defaultValue = "0.1")
	protected double maxSpeed = 0.1;
	
	protected double angleLimit = Math.toRadians(45);
	protected double anglePerSecond = Math.toRadians(90)/10.0;
	
	protected double[] speeds = null;
	protected double[] prevSpeeds = null;
	
	protected double[] rotation = null;
	protected double[] prevRotation = null;
	
	protected double frictionParam = 1.0;

	public MultipleWheelAxesActuatorOld(Simulator simulator, int id, Arguments args, int speeds, int rotations) {
		super(simulator, id, args);
		this.speeds = new double[speeds];
		this.prevSpeeds = new double[speeds];
		
		this.rotation = new double[rotations];
		this.prevRotation = new double[rotations];
		
		frictionParam = args.getArgumentAsDoubleOrSetDefault("friction", frictionParam);
	}
	
	protected void actuateRobot(Robot robot, double timeDelta, double[] speeds, double[] rotations) {
		
		double speedFrontLeft = speeds[0];
		double speedFrontRight = speeds[1];
		double speedBackLeft = speeds[2];
		double speedBackRight = speeds[3];
		
		double rFrontLeft = rotations[0];
		double rFrontRight = rotations[1];
		double rBackLeft = rotations[2];
		double rBackRight = rotations[3];
		
		double rFront = (rFrontLeft+rFrontRight)/2.0;
		double rBack = (rBackLeft+rBackRight)/2.0;
		
		Vector2d position = robot.getPosition();
		double orientation = robot.getOrientation();
		
		Vector2d frontWheelLeft = new Vector2d(position);
		frontWheelLeft.add(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		frontWheelLeft.add(new Vector2d(distanceBetweenWheels/2.0*Math.cos(Math.PI/2),distanceBetweenWheels/2.0*Math.sin(Math.PI/2)));
		
		Vector2d frontWheelRight = new Vector2d(position);
		frontWheelRight.add(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		frontWheelRight.add(new Vector2d(distanceBetweenWheels/2.0*Math.cos(-Math.PI/2),distanceBetweenWheels/2.0*Math.sin(-Math.PI/2)));
		
		Vector2d backWheelLeft = new Vector2d(position);
		backWheelLeft.sub(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		backWheelLeft.sub(new Vector2d(distanceBetweenWheels/2.0*Math.cos(-Math.PI/2),distanceBetweenWheels/2.0*Math.sin(-Math.PI/2)));
		
		Vector2d backWheelRight = new Vector2d(position);
		backWheelRight.sub(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		backWheelRight.sub(new Vector2d(distanceBetweenWheels/2.0*Math.cos(Math.PI/2),distanceBetweenWheels/2.0*Math.sin(Math.PI/2)));
		
		backWheelLeft.add(new Vector2d(timeDelta*speedBackLeft*Math.cos(orientation+rBackLeft),timeDelta*speedBackLeft*Math.sin(orientation+rBackLeft)));
		backWheelRight.add(new Vector2d(timeDelta*speedBackRight*Math.cos(orientation+rBackRight),timeDelta*speedBackRight*Math.sin(orientation+rBackRight)));
		
		frontWheelLeft.add(new Vector2d(timeDelta*speedFrontLeft*Math.cos(orientation+rFrontLeft),timeDelta*speedFrontLeft*Math.sin(orientation+rFrontLeft)));
		frontWheelRight.add(new Vector2d(timeDelta*speedFrontRight*Math.cos(orientation+rFrontRight),timeDelta*speedFrontRight*Math.sin(orientation+rFrontRight)));
		
		Vector2d frontAxle = new Vector2d((frontWheelLeft.x+frontWheelRight.x)/2.0,(frontWheelLeft.y+frontWheelRight.y)/2.0);
		Vector2d backAxle = new Vector2d((backWheelLeft.x+backWheelRight.x)/2.0,(backWheelLeft.y+backWheelRight.y)/2.0);

		Vector2d sum = new Vector2d(frontAxle);
		sum.add(backAxle);
		sum.y/=2;
		sum.x/=2;
		
		double newOrientation = (Math.atan2(frontAxle.y-backAxle.y,frontAxle.x-backAxle.x));

		double changeInOrientation = orientation - newOrientation;
		
		double friction = 0;
		
		friction+= Math.abs(rFrontLeft - rFront) / (angleLimit)*frictionParam;
		friction+= Math.abs(rFrontRight - rFront) / (angleLimit)*frictionParam;
		friction+= Math.abs(rBackLeft - rBack) / (angleLimit)*frictionParam;
		friction+= Math.abs(rBackRight - rBack) / (angleLimit)*frictionParam;
		
		friction = Math.min(friction, 1);
		
//		System.out.println(friction);
		
		Vector2d movement = new Vector2d(sum);
//		movement.x-=position.x;
//		movement.y-=position.y;
		movement.sub(robot.getPosition());
		
//		System.out.println("MOVSUB "+movement);
//		
		Vector2d frictionVec = new Vector2d(movement);
		frictionVec.negate();
		frictionVec.x*=friction;
		frictionVec.y*=friction;
		
		movement.add(frictionVec);
		
//		System.out.println("FRIC "+frictionVec);
//		System.out.println("MOV "+movement);
		
		Vector2d newPosition = new Vector2d(robot.getPosition());
		newPosition.add(movement);
		
		
		robot.setPosition(newPosition);
		
//		double rFront = (rotation[0] + rotation[1])/2.0;
//		double rBack = (rotation[2] + rotation[3])/2.0;
		
//		double frictionFront = (Math.abs(rFrontLeft - rFront) + Math.abs(rFrontRight - rFront))/(2.0*angleLimit);
//		double frictionBack = (Math.abs(rBackLeft - rBack) + Math.abs(rBackRight - rBack))/(2.0*angleLimit);
//		
//		speedFront = speedFront * (1-frictionFront);
//		speedBack= speedBack * (1-frictionBack);
		
//		robot.setPosition(new Vector2d(sum.x/2,sum.y/2));
		robot.setOrientation(newOrientation);
	}
	
	public void setWheelSpeed(int wheelNumber, double val) {
		speeds[wheelNumber] = ( val - 0.5) * maxSpeed * 2.0;
	}
	
	public void setWheelSpeed(double[] vals) {
		for(int i = 0 ; i < vals.length ; i++) {
			setWheelSpeed(i,vals[i]);
		}
	}
	
	public void setRotation(int axisNumber, double val) {
		val = val*angleLimit*2 - angleLimit;
		
		if(val - prevRotation[axisNumber] > anglePerSecond) 
			val = prevRotation[axisNumber] + anglePerSecond;
		
		if(val - prevRotation[axisNumber] < -anglePerSecond) 
			val = prevRotation[axisNumber] - anglePerSecond;
		
		if(val > angleLimit)
			val = angleLimit;
		
		if(val < -angleLimit)
			val = -angleLimit;
		
		rotation[axisNumber] = val;
		prevRotation[axisNumber] = rotation[axisNumber];
	}
	
	public void setRotation(double[] vals) {
		for(int i = 0 ; i < vals.length ; i++) {
			setRotation(i,vals[i]);
		}
	}
	
	public double getMaxSpeed() {
		return maxSpeed;
	}
	
	public double[] getSpeed(){
		return speeds;
	}
	
	public double[] getRotation() {
		return rotation;
	}
	
	public int getNumberOfSpeeds() {
		return speeds.length;
	}
	
	public int getNumberOfRotations() {
		return rotation.length;
	}

}
