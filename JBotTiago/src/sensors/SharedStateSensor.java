package sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.SimplePreySensor;
import simulation.util.Arguments;

public class SharedStateSensor extends Sensor {

	private Simulator simulator;
	private double readingsContribution;

	public SharedStateSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		readingsContribution = 0;
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		super.update(time, teleported);
		for (Robot r : simulator.getRobots()) {
			if (robot.getId() != r.getId()) {
				SimplePreySensor sensor = (SimplePreySensor) r.getSensorByType(SimplePreySensor.class);
				double higherReading = 0;
				for (int i = 0; i < sensor.getNumberOfSensors(); i++) {
					if (sensor.getSensorReading(i) > higherReading) {
						higherReading = sensor.getSensorReading(i);
					}
				}
				readingsContribution += higherReading;
			}
		}
		readingsContribution /= simulator.getRobots().size();
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return readingsContribution;
	}

}
