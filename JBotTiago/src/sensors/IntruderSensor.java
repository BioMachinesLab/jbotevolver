package sensors;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowPreyChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class IntruderSensor extends ConeTypeSensor {
	
	private boolean foundIntruder;
	private double intruderOrientation;
	private Vector2d estimatedValue;
	private double metersAhead;

	public IntruderSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowPreyChecker(robot.getId()));
		metersAhead = args.getArgumentAsDoubleOrSetDefault("metersahead", 0.5);
	}
	
	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j = 0; j < readings.length; j++){
			if(openingAngle > 0.018){ //1degree
				readings[j] = calculateContributionToSensor(j, source);
			}
		}
	}

	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {
		
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		foundIntruder = false;
		
		if((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			foundIntruder = true;
			calculateOrientation(sensorInfo);
			calculateEstimatedIntruderPosition();
			return ((openingAngle/2) - Math.abs(intruderOrientation)) / (openingAngle/2);
		}else
			return 0;		
	}

	private void calculateOrientation(GeometricInfo sensorInfo) {
		intruderOrientation = sensorInfo.getAngle();
	}
	
	private void calculateEstimatedIntruderPosition(){
		double alpha = robot.getOrientation() + getIntruderOrientation();
		
		double x = robot.getPosition().x + (Math.cos(alpha) * metersAhead);
		double y = robot.getPosition().y + (Math.sin(alpha) * metersAhead);
		
		estimatedValue = new Vector2d(x, y);
	}
	
	public boolean foundIntruder() {
		return foundIntruder;
	}

	public double getIntruderOrientation() {
		return intruderOrientation;
	}

	public Vector2d getEstimatedIntruder() {
		return estimatedValue;
	}
	
}
