package simulation.robot.sensors;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.Wall.Edge;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class WallSensor extends ConeTypeSensor{

	public WallSensor(Simulator simulator, int id, Robot robot, Arguments args){
			/*int numbersensors, double orientation, double range,
			AllowedObjectsChecker allowedObjectsChecker) {*/
		super(simulator, id, robot, args);
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		//distance to the closest point in the wall, not the center
		Wall w = (Wall) source.getObject();
		
		Edge[] edges = w.getEdges();
		Vector2d wallCenter = w.getPosition(), robotPos = robot.getPosition();
		
		Vector2d closestPoint = null;
		for(Edge edge : edges){
			Vector2d intersection = simulation.util.MathUtils.calculateIntersectionPoint(
					wallCenter, robotPos, edge.getP1(), edge.getP2());
			//(robot, wall center) segment only intersects one edge
			if(intersection != null){
				closestPoint = intersection;
			}
		}
		//closest point "object"
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, closestPoint);
		double distance = sensorInfo.getDistance();
				
		if(distance < getRange()) {
			return (getRange() - sensorInfo.getDistance()) / getRange();

		}
		return 0;
	}

}
