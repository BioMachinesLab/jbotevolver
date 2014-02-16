package sensors;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowPreyChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class SharedStateSensor extends ConeTypeSensor {

	private Simulator simulator;
	private IntruderSensor sensors[];

	public SharedStateSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		setAllowedObjectsChecker(new AllowPreyChecker(robot.getId()));
	}

	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {

		int robotsSeeingIntruder = 0;
		
		if(sensors == null) {
			sensors = new IntruderSensor[simulator.getRobots().size()];
			
			for (int j = 0; j < simulator.getRobots().size(); j++) {
				IntruderSensor s = (IntruderSensor)simulator.getRobots().get(j).getSensorByType(IntruderSensor.class);
				sensors[j] = s;
			}	
		}
		
		IntruderSensor valid = null;

		for(int j = 0 ; j < sensors.length ; j++){
			IntruderSensor s = sensors[j];
			
			if (s.foundIntruder()) {
				valid = s;
				robotsSeeingIntruder++;
			}
			
			if(robotsSeeingIntruder >= 2)
				break;
		}

		if (robotsSeeingIntruder == 0) {
			return 0;
		} else if (robotsSeeingIntruder == 1) {
			Vector2d estimation = valid.getEstimatedIntruder();
			if(estimation==null)
				System.out.println();
			return calculateValue(i,estimation);
		} else {
			return calculateValue(i,source.getObject().getPosition());
		}
	}

	private double calculateValue(int i, Vector2d source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		if ((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			return (getRange() - sensorInfo.getDistance()) / getRange();
		}
		
		return 0;
	}
	
}
