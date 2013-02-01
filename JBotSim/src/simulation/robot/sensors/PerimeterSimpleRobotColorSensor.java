package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PerimeterSimpleRobotColorSensor extends SimpleRobotColorSensor {

	protected Double time;
	protected GeometricCalculator calc;
	
	public PerimeterSimpleRobotColorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
		time = simulator.getTime();
		calc = simulator.getGeoCalculator();
	}

	@Override
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			PhysicalObjectDistance source) {
		double orientation = angles[sensorNumber] + robot.getOrientation();
		sensorPosition.set(Math.cos(orientation) * robot.getRadius()
				+ robot.getPosition().getX(),
				Math.sin(orientation) * robot.getRadius()
						+ robot.getPosition().getY());
		// sensorPosition.set(robot.getPosition().getX(),
		// robot.getPosition().getY());
		GeometricInfo sensorInfo = calc.getGeometricInfoBetween(
				sensorPosition, orientation, source.getObject(), time);
		return sensorInfo;
	}

}
