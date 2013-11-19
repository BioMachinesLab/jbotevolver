package actuator;

import sensors.ChangeablePreySensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class ChangeIndividualSensorActuator  extends Actuator {
	
	private double range;
	private double openingAngle;
	private double orientation;
	private double maxRange;
	private double maxOpeningAngle;
	private ChangeablePreySensor sensor;
	private int idSensor;
	private boolean changeRange;
	private boolean changeAngle;
	private boolean changeOrientation;
	private int numberOfOutputs;
	private int numberOfSensors;

	public ChangeIndividualSensorActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		idSensor = args.getArgumentAsIntOrSetDefault("idsensor", -1);
		changeRange = args.getArgumentAsIntOrSetDefault("changerange", 0)==1;
		changeAngle = args.getArgumentAsIntOrSetDefault("changeangle", 0)==1;
		changeOrientation = args.getArgumentAsIntOrSetDefault("changeorientation", 0)==1;
		numberOfSensors = args.getArgumentAsIntOrSetDefault("numberofsensors", 8);
		
		if(changeAngle)
			numberOfOutputs++;
		if(changeRange)
			numberOfOutputs++;
		if(changeOrientation)
			numberOfOutputs++;

		numberOfOutputs *= numberOfSensors;
		
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
			sensor = (ChangeablePreySensor)robot.getSensorWithId(idSensor);
			maxRange = sensor.getRangeAtIndex(idSensor);
			maxOpeningAngle = sensor.getOpeningAngleAtIndex(idSensor);
		}
		
		if(changeRange){
			sensor.setRangeAtIndex(idSensor, range * maxRange);
//			System.out.print(sensor.getRangeAtIndex(idSensor) + " ");
		}
		
		if(changeAngle){
			sensor.setOpeningAngleAtIndex(idSensor, openingAngle * maxOpeningAngle);
//			System.out.print(Math.toDegrees(sensor.getOpeningAngleAtIndex(idSensor)) + " ");
		}
		
		if(changeOrientation){
			double delta = 2 * Math.PI / sensor.getNumberOfSensors();
			double angle = 0;
			double deviation = (orientation * Math.PI)-(Math.PI/2);
			for (int i=0;i< sensor.getNumberOfSensors();i++){
				sensor.getAngles()[i] = angle + deviation;
				angle+=delta;
			}
		}
	}

}
