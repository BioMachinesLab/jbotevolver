package sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.SimplePreySensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import utils.SensorEstimation;

public class SimplePreySharedSensor extends SimplePreySensor {

	private Simulator simulator;
	private double metersAhead;
	private GeometricCalculator geometricCalculator;
	private LinkedList<SensorEstimation> sensorEstimations;
	private ArrayList<Robot> robots;
	private PreyPickerActuator picker;
	@ArgumentsAnnotation(name="shareoneprey", values={"0","1"}, help="Allow robot to share information when only sensing one prey")
	private boolean shareOnePrey;

	public SimplePreySharedSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		metersAhead = robot.getRadius() * 10;

		robots = simulator.getRobots();
		geometricCalculator = new GeometricCalculator();
		sensorEstimations = new LinkedList<SensorEstimation>();
		
		shareOnePrey = args.getArgumentAsIntOrSetDefault("shareoneprey", 0)==1;
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {

		if (simulator.getTime() == 0) {
			picker = (PreyPickerActuator) robot.getActuatorByType(PreyPickerActuator.class);
		}

		clearEstimationFromCloseRobots();
		sensorEstimations.clear();
		
		super.update(time, teleported);

		if (sensorEstimations != null) {
			sendEstimationToCloseRobots();
		}

	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);

		if ((sensorInfo.getDistance() < getCutOff())
				&& (sensorInfo.getAngle() < (openingAngle / 2.0))
				&& (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			double orientation = geometricCalculator
					.getGeometricInfoBetweenPoints(robot.getPosition(),
							robot.getOrientation(),
							source.getObject().getPosition(),
							simulator.getTime()).getAngle();
			
			sensorEstimations.add(new SensorEstimation((source.getObject().getId() - Simulator.maxNumberRobots),
					calculateEstimatedIntruderPosition(orientation,source.getObject())));

			return (getRange() - sensorInfo.getDistance()) / getRange();
		}
		return 0;
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for (int j = 0; j < numberOfSensors; j++) {
			readings[j] = Math.max(calculateContributionToSensor(j, source),
					readings[j]);
		}
	}

	private void clearEstimationFromCloseRobots() {
		for (Robot r : robots) {
			SharedSensor sharedSensor = (SharedSensor) ((Robot) r)
					.getSensorByType(SharedSensor.class);
			if (sharedSensor == null)
				return;
			sharedSensor.clearEstimations(this.robot.getId(), sensorEstimations);
		}
	}

	private void sendEstimationToCloseRobots() {
		for (Robot r : robots) {
			SharedSensor sharedSensor = (SharedSensor) ((Robot) r).getSensorByType(SharedSensor.class);
			if(shareOnePrey)
				sharedSensor.addEstimation(this.robot.getId(), sensorEstimations);
			else{
				if (sharedSensor == null || !picker.isCarryingPrey() && sensorEstimations.size() == 1)
					return;
				sharedSensor.addEstimation(this.robot.getId(), sensorEstimations);
			}

		}
	}

	private Vector2d calculateEstimatedIntruderPosition(double orientation,
			PhysicalObject obj) {
		double alpha = robot.getOrientation() - orientation;

		double x = robot.getPosition().x
				+ (FastMath.cosQuick(alpha) * (metersAhead + robot.getRadius()));
		double y = robot.getPosition().y
				+ (FastMath.sinQuick(alpha) * (metersAhead + robot.getRadius()));

		return new Vector2d(x, y);
	}

}
