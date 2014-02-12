package sensors;

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

	public IntruderSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowPreyChecker(robot.getId()));
	}

	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {
		
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		foundIntruder = false;
		
		if((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			foundIntruder = true;
			calculateOrientation(sensorInfo);
			System.out.println("1");
			System.out.println(Math.toDegrees(intruderOrientation));
			return 1;
		}else
			System.out.println("0");
			return 0;
		
		
	}

	private void calculateOrientation(GeometricInfo sensorInfo) {
		intruderOrientation = sensorInfo.getAngle();
	}
	
	public boolean foundIntruder() {
		return foundIntruder;
	}

	public double getIntruderOrientation() {
		return intruderOrientation;
	}
	
}
