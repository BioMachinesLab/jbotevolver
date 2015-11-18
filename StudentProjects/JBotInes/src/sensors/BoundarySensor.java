package sensors;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.checkers.AllowArbitraryChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class BoundarySensor extends WallRaySensor{

	public BoundarySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		AllowArbitraryChecker checker = new AllowArbitraryChecker();
		checker.add(PhysicalObjectType.LINE);
		setAllowedObjectsChecker(checker);
	}
	
	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
		double inputValue = 0;
		
		if(source.getObject().getType() != PhysicalObjectType.ROBOT) {
		
			Line l = (Line) source.getObject();
			
			for(int i = 0 ; i < numberOfRays ; i++) {
				Vector2d cone = cones[sensorNumber][i];
				
				rayPositions[sensorNumber][i][0] = sensorPositions[sensorNumber];
				if(rayPositions[sensorNumber][i][1] == null)
					rayPositions[sensorNumber][i][1] = cone;
				
				Vector2d intersection = null;
				intersection = l.intersectsWithLineSegment(sensorPositions[sensorNumber], cone);
				
				if(intersection != null) {
					double distance = intersection.distanceTo(sensorPositions[sensorNumber]);
					closestDistance = distance < closestDistance ? distance : closestDistance;
					cone.angle(intersection);
					
					if(distance < range) {
						inputValue = (range-distance)/range;
						
						if(inputValue > rayReadings[sensorNumber][i]) {
							rayPositions[sensorNumber][i][1] = intersection;
							rayReadings[sensorNumber][i] = Math.max(inputValue, rayReadings[sensorNumber][i]);
						}
					}
				}
			}
		}
		return inputValue;
	}
	
}
