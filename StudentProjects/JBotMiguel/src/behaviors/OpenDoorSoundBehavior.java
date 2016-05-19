package behaviors;

import behaviors.OpenDoorBehavior;
import roommaze.SoundActuator;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class OpenDoorSoundBehavior extends OpenDoorBehavior{
	
	private int soundTime = 10;

	public OpenDoorSoundBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		soundTime = args.getArgumentAsIntOrSetDefault("soundtime", soundTime);
	}

	@Override
	public void controlStep(double time) {
		super.controlStep(time);
		if(openedDoor && soundTime > 0) {
			Actuator a = robot.getActuatorByType(SoundActuator.class);
			if(a != null) {
				SoundActuator sa = (SoundActuator)a;
				sa.playSoundTimesteps(soundTime, 1.0);
			}
		}
	}
}