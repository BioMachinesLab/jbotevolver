package sensors;

import java.util.ArrayList;

import phisicalObjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowPreyChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class IntensityPreySensor extends ConeTypeSensor {

	private ArrayList<Robot> robots;
	private Simulator simulator;
	private SharedInformation information = null;

	public IntensityPreySensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		setAllowedObjectsChecker(new AllowPreyChecker(id));
		robots = simulator.getRobots();

	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for (int j = 0; j < readings.length; j++) {
			if (openingAngle > 0.018) { // 1degree
				readings[j] = calculateContributionToSensor(j, source);
			}
		}
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		super.update(time, teleported);
		if (information != null) {
			sendInformation(information);
			
			information = null;
		}

	}

	private void sendInformation(SharedInformation information) {
		SharedIntensitySensor sharedSensor = (SharedIntensitySensor) this.robot
				.getSensorByType(SharedIntensitySensor.class);
		sharedSensor.addInformation(information);
		//System.out.println("intensity adicionou info");
	}

	@Override
	protected double calculateContributionToSensor(int i,
			PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);

		if ((sensorInfo.getDistance() < getCutOff())
				&& (sensorInfo.getAngle() < (openingAngle / 2.0))
				&& (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			
			IntensityPrey prey = (IntensityPrey) source.getObject();
			information = new SharedInformation(prey.getPosition(), prey.getIntensity(), simulator.getTime());

			return ((openingAngle / 2) - Math.abs(sensorInfo.getAngle()))
					/ (openingAngle / 2);
		} else
			return 0;
	}


}
