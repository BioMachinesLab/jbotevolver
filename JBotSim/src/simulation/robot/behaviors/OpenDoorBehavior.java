package simulation.robot.behaviors;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorBehavior extends Behavior{
	
	protected TwoRoomsEnvironment env;
	protected double distanceToDoor = 0.2;
	protected boolean openedDoor = false;
	protected Robot robot;

	public OpenDoorBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		env = (TwoRoomsEnvironment)simulator.getEnvironment();
		distanceToDoor = args.getArgumentAsDoubleOrSetDefault("distancetodoor", distanceToDoor);
		this.robot = r;
	}

	@Override
	public void controlStep(double time) {
		openedDoor = false;
		LinkedList<Wall> buttons = env.getButtons();
		Vector2d r = robot.getPosition(); 
		
		for(Wall b : buttons) {
			Vector2d bPos = b.getPosition();
			if(bPos.distanceTo(r) < distanceToDoor) {
				openedDoor = env.openDoor();
				break;
			}
		}
		((DifferentialDriveRobot) robot).setWheelSpeed(0, 0);
	}
}