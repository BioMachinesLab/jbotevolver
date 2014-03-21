package sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class IntruderSensor extends ConeTypeSensor {
	
	private Vector2d estimatedValue;
	private double metersAhead;
	private LinkedList<PhysicalObject> estimatedIntruders = new LinkedList<PhysicalObject>();

	public IntruderSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowMouseChecker(id));
		metersAhead = robot.getRadius()*10;
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
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		estimatedIntruders.clear();
		super.update(time, teleported);
	}

	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {
		
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		
		if((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			double o = calculateOrientation(sensorInfo,angles[i]);
			calculateEstimatedIntruderPosition(i,o,source.getObject());
			return ((openingAngle/2) - Math.abs(sensorInfo.getAngle())) / (openingAngle/2);
		}else
			return 0;		
	}

	private double calculateOrientation(GeometricInfo sensorInfo, double offset) {
		if(offset > Math.PI)
			offset-=Math.PI*2;
		return sensorInfo.getAngle() - offset;
	}
	
	private void calculateEstimatedIntruderPosition(int sensor, double orientation, PhysicalObject obj){
		double alpha = robot.getOrientation() + orientation;
		
		double x = robot.getPosition().x + robot.getRadius()*Math.cos(alpha) + (Math.cos(alpha) * metersAhead);
		double y = robot.getPosition().y + robot.getRadius()*Math.sin(alpha) + (Math.sin(alpha) * metersAhead);
		
		estimatedValue = new Vector2d(x, y);
		estimatedIntruders.add(obj);
	}
	
	public boolean foundIntruder() {
		return !estimatedIntruders.isEmpty();
	}

	public LinkedList<PhysicalObject> getEstimatedIntruder() {
		return estimatedIntruders;
	}
	
}
