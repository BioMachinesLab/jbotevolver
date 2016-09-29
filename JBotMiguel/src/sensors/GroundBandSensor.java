package sensors;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.GroundBand;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class GroundBandSensor extends Sensor {
	
	private Simulator sim;
	private GeometricCalculator calc = new GeometricCalculator();
	
	public GroundBandSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.sim = simulator;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		
		double highestReading = 0;
		
		for(PhysicalObject o : sim.getEnvironment().getAllObjects()) {
			if(o instanceof GroundBand) {
				GroundBand gc = (GroundBand)o;
				highestReading = Math.max(highestReading, calculateReading(gc));
			}
		}
		
		return highestReading;
	}
	
	private double calculateReading(GroundBand gc) {
		
		Vector2d gcPos = gc.getPosition();
		Vector2d robotPos = robot.getPosition();
		
		GeometricInfo info = calc.getGeometricInfoBetweenPoints(gcPos,0.0,robotPos,0.0);
		
		double angle = -info.getAngle();
		double distance = info.getDistance();
		
		if(angle < 0)
			angle = 2*Math.PI+angle;
		
		if(distance > gc.getOuterRadius() || distance < gc.getInnerRadius())
			return 0;
		
		if(angle < gc.getStartOrientation() || angle > gc.getEndOrientation())
			return 0;
		
		double scale = gc.getOuterRadius()-gc.getInnerRadius();
		double difference = distance - gc.getInnerRadius();
		return 1 - difference/scale;
	}

}
