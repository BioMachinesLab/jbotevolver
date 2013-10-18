package actuator;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class ChangeSensorActuator extends Actuator {
	
	private double range;
	private double openingAngle;
	private double orientation;
	private double maxRange;
	private double maxOpeningAngle;
	private ConeTypeSensor sensor;
	private int idSensor;
	private boolean changeRange;
	private boolean changeAngle;
	private boolean changeOrientation;
	private int numberOfOutputs;
	private boolean cutOff;

	public ChangeSensorActuator(Simulator simulator, int id, Arguments args) {
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
		}
		
		if(changeAngle)
			sensor.setOpeningAngle(openingAngle * maxOpeningAngle);
		
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
