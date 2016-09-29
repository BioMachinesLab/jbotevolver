package behaviors;

import behaviors.Behavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.robot.sensors.PreySensor;
import simulation.util.Arguments;

public class ColorNearPreyBehavior extends Behavior {
	
	private PreySensor sensor;
	private RobotRGBColorActuator actuator;
	
	public ColorNearPreyBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		sensor = (PreySensor)robot.getSensorByType(PreySensor.class);
		actuator = (RobotRGBColorActuator)robot.getActuatorByType(RobotRGBColorActuator.class);
	}

	public void controlStep(double time) {
		double max = 0;
		for(int i = 0 ; i < sensor.getNumberOfSensors() ; i++) {
			double s = sensor.getSensorReading(i);
			max = s > max ? s : max;
		}
		actuator.setRed(max);
	}
}