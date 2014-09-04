package simulation.robot.sensors;

import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PerimeterSimpleLightTypeSensor extends SimpleLightTypeSensor {

	protected Double time;
	protected GeometricCalculator calc;
	
	public PerimeterSimpleLightTypeSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id, robot, args);
		setAllowedObjectsChecker(new AllowLightChecker());
		time = simulator.getTime();
		calc = new GeometricCalculator();
	}

	@Override
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			PhysicalObjectDistance source) {
		double orientation=angles[sensorNumber]+robot.getOrientation();
		sensorPosition.set(FastMath.cosQuick(orientation)*robot.getRadius()+robot.getPosition().getX(),
				FastMath.sinQuick(orientation)*robot.getRadius()+robot.getPosition().getY());
//		sensorPosition.set(robot.getPosition().getX(), robot.getPosition().getY());
		GeometricInfo sensorInfo = calc.getGeometricInfoBetween(sensorPosition, orientation, source.getObject(),
				time);
		return sensorInfo;
	}

}
