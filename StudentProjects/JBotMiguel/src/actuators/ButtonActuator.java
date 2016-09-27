package actuators;

import java.util.LinkedList;

import mathutils.Vector2d;
import roommaze.SoundActuator;
import roommaze.TwoRoomsEnvironment;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class ButtonActuator extends Actuator {
	
	private Simulator simulator;
	private double distanceToDoor = 0.2;
	private int soundTime = 10;
	private double activation = 0;
	private int stopTime = 0;

	public ButtonActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.simulator = simulator;
		distanceToDoor = args.getArgumentAsDoubleOrSetDefault("distancetodoor", distanceToDoor);
		soundTime = args.getArgumentAsIntOrSetDefault("soundtime", soundTime);
		stopTime = args.getArgumentAsIntOrSetDefault("stoptime", 0);
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
		
		if(activation > 0.5) {
			TwoRoomsEnvironment env = (TwoRoomsEnvironment)simulator.getEnvironment();
			
			boolean openedDoor = false;
			LinkedList<Wall> buttons = env.getButtons();
			Vector2d r = robot.getPosition(); 
			
			for(Wall b : buttons) {
				Vector2d bPos = b.getPosition();
				if(bPos.distanceTo(r) < distanceToDoor) {
					openedDoor = env.openDoor(bPos.getX());
					break;
				}
			}
			
			((DifferentialDriveRobot) robot).stopTimestep(stopTime);
			
			if(openedDoor && soundTime > 0) {
				Actuator a = robot.getActuatorByType(SoundActuator.class);
				if(a != null) {
					SoundActuator sa = (SoundActuator)a;
					sa.playSoundTimesteps(soundTime, 1.0);
				}
			}
		}
	}

	public void setActivation(double value) {
		this.activation = value;
	}
}