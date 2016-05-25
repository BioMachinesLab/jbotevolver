package roommaze;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class SoundActuator extends Actuator{
	
	private double volume = 0;
	private double maxVolume = 1;
	private boolean onOffSensor;
	
	private int soundTimesteps = 0;
	private double desiredVolume = 0;
	
	public SoundActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		maxVolume = args.getArgumentAsDoubleOrSetDefault("maxvolume", maxVolume);
		onOffSensor = args.getArgumentAsIntOrSetDefault("onoff", 1) == 1;
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
		if(soundTimesteps > 0) {
			soundTimesteps--;
			setVolume(desiredVolume);
			if(soundTimesteps == 0)
				setVolume(0);
		}
	}
	
	public void setActivation(double activation) {
		if(onOffSensor) {
			if(activation > 0.5)
				volume = 1;
			else
				volume = 0;
		} else {
			volume = activation*maxVolume;
		}
	}

	public void setVolume(double v) {
		if(v > maxVolume)
			volume = maxVolume;
		else if (v < 0)
			volume = 0;
		else
			volume = v;
	}
	
	public double getVolume() {
		return volume;
	}
	
	public double getMaxVolume() {
		return maxVolume;
	}

	public void playSoundTimesteps(int soundTime, double volumePercentage) {
		desiredVolume = volumePercentage*maxVolume;
		soundTimesteps = soundTime;
	}
}