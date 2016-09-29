package sensors;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class WaypointSensor extends Sensor {
	
	private Simulator sim;
	@ArgumentsAnnotation(name="distance", defaultValue="1")
	private double distance = 1;
	private String destination = "";
	private LightPole destinationLightPole = null;
	protected GeometricCalculator calc;
	
	public WaypointSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.sim = simulator;
		distance = args.getArgumentAsDoubleOrSetDefault("distance",distance);
		this.calc = new GeometricCalculator();
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		
		Environment env = sim.getEnvironment();
		
		LightPole dest = null;
		
		if(destination.isEmpty()) {
			
			for(PhysicalObject o : env.getAllObjects()) {
				if(o.getType() == PhysicalObjectType.LIGHTPOLE) {
					if(dest == null)
						dest = (LightPole)o;
					if(robot.getPosition().distanceTo(dest.getPosition()) > robot.getPosition().distanceTo(o.getPosition()))
						dest = (LightPole)o;
				}
			}
			
		} else {
			
			if(destinationLightPole == null || !destinationLightPole.getName().equals(destination)) {
				for(PhysicalObject o : env.getAllObjects()) {
					if(o.getType() == PhysicalObjectType.LIGHTPOLE && o.getName().equals(destination)) {
						dest = (LightPole)o;
						destinationLightPole = dest;
						break;
					}
				}
			} else {
				dest = destinationLightPole;
			}
		}
		
		double value = 0;
		
		if(dest != null) {
			
			GeometricInfo sensorInfo = calc.getGeometricInfoBetween(robot.getPosition(), robot.getOrientation(), dest, sim.getTime());
			
			if(sensorNumber == 0) { //range
				if (sensorInfo.getDistance() < distance)
					value = (distance - sensorInfo.getDistance())/distance;
			} else if(sensorNumber == 1) { //angle
				value = sensorInfo.getAngle() / Math.PI / 2 + 0.5;
			} else
				value = 0.5;
		}
		return value;
	}
	
	public void setDestination(String s) {
		this.destination = s;
	}
}