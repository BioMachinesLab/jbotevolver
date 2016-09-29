package sensors;

import roommaze.SoundActuator;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class SoundSensor extends Sensor {
	
	private Simulator simulator;
	
	public SoundSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.simulator = simulator;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		double maxSound = 0;
		for(Robot r : simulator.getRobots()) {
			Actuator actuator = r.getActuatorByType(SoundActuator.class);
			if(actuator != null) {
				SoundActuator sound = (SoundActuator)actuator;
				double reading =  sound.getVolume()/sound.getMaxVolume();
				if(maxSound <  reading)
					maxSound = reading;
			}
		}
		return maxSound;
	}
}