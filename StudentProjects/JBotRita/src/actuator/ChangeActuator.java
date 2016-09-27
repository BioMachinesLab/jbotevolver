package actuator;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class ChangeActuator extends Actuator {
	
	private double range = 1;
	private double openingAngle = 1;
	private double orientation;
	private double maxRange;
	private double maxOpeningAngle;
	private ConeTypeSensor sensor;
	@ArgumentsAnnotation(name="idsensor", help="Id of the sensor that will share information.", defaultValue="1")
	private int idSensor;
	@ArgumentsAnnotation(name="changerange", help="Set to 1 to let the robot change the range of the selected sensor.", values={"0","1"})
	private boolean changeRange;
	@ArgumentsAnnotation(name="changeangle", help="Set to 1 to let the robot change the openning angle of the selected sensor.", values={"0","1"})
	private boolean changeAngle;
	@ArgumentsAnnotation(name="changeorientation", help="Set to 1 to let the robot change the orientation of the selected sensor.", values={"0","1"})
	private boolean changeOrientation;
	private int numberOfOutputs;
	@ArgumentsAnnotation(name="cutoff", help="Id of the sensor that will share information.", values={"0","1"})
	private boolean cutOff;

	public ChangeActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		idSensor = args.getArgumentAsIntOrSetDefault("idsensor", -1);
		changeRange = args.getArgumentAsIntOrSetDefault("changerange", 0)==1;
		changeAngle = args.getArgumentAsIntOrSetDefault("changeangle", 0)==1;
		changeOrientation = args.getArgumentAsIntOrSetDefault("changeorientation", 0)==1;
		cutOff = args.getArgumentAsIntOrSetDefault("cutoff", 0)==1;
		
		if(changeAngle)
			numberOfOutputs++;
		if(changeRange)
			numberOfOutputs++;
		if(changeOrientation)
			numberOfOutputs++;
			
	}
	
	public int getNumberOfOutputs() {
		return numberOfOutputs;
	}
	
	public boolean isChangeAngle() {
		return changeAngle;
	}
	
	public boolean isChangeRange() {
		return changeRange;
	}
	
	public boolean isChangeOrientation() {
		return changeOrientation;
	}
	
	public void setRange(double range) {
		this.range = range;
	}
	
	public void setOpeningAngle(double openingAngle) {
		this.openingAngle = openingAngle;
	}
	
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	@Override
	public void apply(Robot robot) {
		if(sensor == null){
			sensor = (ConeTypeSensor)robot.getSensorWithId(idSensor);
			maxRange = sensor.getRange();
			maxOpeningAngle = sensor.getOpeningAngle();
		}
		
		if(changeRange){
			if(!cutOff)
				sensor.setRange(range * maxRange);

			sensor.setCutOff(range * maxRange);
			
//			System.out.print(sensor.getRange() + " ");
		}
		
		if(changeAngle){
			sensor.setOpeningAngle(openingAngle * maxOpeningAngle);
//			System.out.print(Math.toDegrees(sensor.getOpeningAngle()) + " ");
		}
		
		if(changeOrientation){
			double deviation = (orientation * Math.PI)-(Math.PI/2);
			for (int i=0;i< sensor.getNumberOfSensors();i++){
				sensor.getAngles()[i] = sensor.getOriginalAngles()[i] + deviation;
			}
		}
		
	}

}