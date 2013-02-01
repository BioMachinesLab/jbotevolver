package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.BehaviorActuator;
import simulation.util.Arguments;

public class BehaviorSensor extends Sensor{

	private int behaviorActuatorNumber;
	private boolean binary;
	
	public BehaviorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id, robot, args);

		this.behaviorActuatorNumber = (args.getArgumentIsDefined("actuator")) ? args.getArgumentAsInt("actuator") : 0;
		this.binary = args.getArgumentIsDefined("binary");
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		BehaviorActuator b = (BehaviorActuator)robot.getActuatorWithId(behaviorActuatorNumber);
		
		if(binary)
			return b.getActiveBehaviorIndex() == sensorNumber ? 1 : 0;
		else
			return b.getValue(sensorNumber);
	}

	@Override
	public String toString() {
		BehaviorActuator b = (BehaviorActuator)robot.getActuatorWithId(behaviorActuatorNumber);
		return "BehaviorSensor "+b.getActiveBehaviorIndex();
	}
	
	public int getNumberOfBehaviors() {
		BehaviorActuator b = (BehaviorActuator)robot.getActuatorWithId(behaviorActuatorNumber);
		return b.getNumberOfBehaviors();
	}

	
}
