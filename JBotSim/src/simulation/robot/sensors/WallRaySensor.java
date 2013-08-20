package simulation.robot.sensors;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.checkers.AllowWallChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

@SuppressWarnings("serial")
public class WallRaySensor extends ConeTypeSensor {

	private int numberOfRays = 7;
	private double[][] rayReadings;
	
	protected Random random;
	private Vector2d[][] cones;
	private Vector2d[] sensorPositions;
	private double fullOpeningAngle;
	private double minimumDistances[][];
	private double cutoffAngle = 90;
	
	public Vector2d[][][] rayPositions;
	
	public WallRaySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.random = simulator.getRandom();
		
		numberOfRays = args.getArgumentAsIntOrSetDefault("numberofrays", numberOfRays);
		cutoffAngle = args.getArgumentAsDoubleOrSetDefault("cutoffangle", cutoffAngle);
		
		if(numberOfRays%2 == 0)
			numberOfRays++;
		
		rayReadings = new double[numberOfSensors][numberOfRays];
		
		this.fullOpeningAngle = openingAngle;
		this.openingAngle = openingAngle / 2;
		
		setAllowedObjectsChecker(new AllowWallChecker());
		
		sensorPositions = new Vector2d[numberOfSensors];
		cones = new Vector2d[numberOfSensors][numberOfRays];
		for(int i = 0 ; i < numberOfSensors ; i++) {
			sensorPositions[i] = new Vector2d();
			for(int j = 0 ; j < numberOfRays ; j++)
				cones[i][j] = new Vector2d();
		}
		minimumDistances = new double[numberOfSensors][numberOfRays];
	}
	
	private void updateCones() {
		
		rayPositions = new Vector2d[numberOfSensors][numberOfRays][2];
		minimumDistances = new double[numberOfSensors][numberOfRays];
		
		for(int sensorNumber = 0 ; sensorNumber < numberOfSensors ; sensorNumber++) {
			double orientation = angles[sensorNumber] + robot.getOrientation();
			
			sensorPositions[sensorNumber].set(
					Math.cos(orientation) * robot.getRadius() + robot.getPosition().getX(),
					Math.sin(orientation) * robot.getRadius() + robot.getPosition().getY()
				);
			
			double alpha = (this.fullOpeningAngle)/(numberOfRays-1);
			
			for(int i = 0 ; i < numberOfRays ; i++) {
				
				cones[sensorNumber][i].set(
						Math.cos(orientation - openingAngle + alpha*i)* range + sensorPositions[sensorNumber].getX(),
						Math.sin(orientation - openingAngle + alpha*i)* range + sensorPositions[sensorNumber].getY()
					 );
			}
		}
	}

	/**
	 * Calculates the distance between the robot and a given PhisicalObject if
	 * in range
	 * 
	 * @return distance or 0 if not in range
	 */
	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
			
		double inputValue = 0;
		
		if(source.getObject().getType() != PhysicalObjectType.ROBOT) {
		
			Wall w = (Wall) source.getObject();
			
			for(int i = 0 ; i < numberOfRays ; i++) {
				Vector2d cone = cones[sensorNumber][i];
				
				rayPositions[sensorNumber][i][0] = sensorPositions[sensorNumber];
				if(rayPositions[sensorNumber][i][1] == null)
					rayPositions[sensorNumber][i][1] = cone;
				
				Vector2d intersection = null;
				intersection = w.intersectsWithLineSegment(sensorPositions[sensorNumber], cone, Math.toRadians(cutoffAngle));
				
				if(intersection != null) {
					
					double distance = intersection.distanceTo(sensorPositions[sensorNumber]);
					cone.angle(intersection);
					
					if(distance < range) {
						inputValue = (range-distance)/range;
						
						if((minimumDistances[sensorNumber][i] == 0 || distance < minimumDistances[sensorNumber][i]) && inputValue > rayReadings[sensorNumber][i]) {
							rayPositions[sensorNumber][i][1] = intersection;
							rayReadings[sensorNumber][i] = Math.max(inputValue, rayReadings[sensorNumber][i]);
						}
					}
				}
			}
		}
		return inputValue;
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		updateCones();

		if(closeObjects != null) {
			closeObjects.update(time, teleported);
		}
	
		try { 
			for(int i = 0; i < numberOfSensors; i++){
				for(int j = 0; j < numberOfRays; j++){
					rayReadings[i][j] = 0.0;
				}
				readings[i] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
					iterator.updateCurrentDistance(geoCalc.getDistanceBetween(robot.getPosition(), source.getObject(),time));
				}
			}
			
			for(int i = 0; i < numberOfSensors; i++){
				double avg = 0;
				for(int ray = 0 ; ray < numberOfRays ; ray++)
					avg+= rayReadings[i][ray]/numberOfRays;
				readings[i]=avg;
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++){
			calculateContributionToSensor(j, source);
		}
	}
	
	@Override
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			Vector2d source) {
		double orientation = angles[sensorNumber] + robot.getOrientation();
		sensorPosition.set(Math.cos(orientation) * robot.getRadius()
				+ robot.getPosition().getX(),
				Math.sin(orientation) * robot.getRadius()
						+ robot.getPosition().getY());

		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(sensorPosition, 
				orientation,source, time);

		return sensorInfo;
	}

	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "WallRaySensor [readings=" + Arrays.toString(readings) + "]";
	}
}