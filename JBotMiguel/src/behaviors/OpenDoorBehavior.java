package behaviors;

import java.util.LinkedList;

import mathutils.Vector2d;
import roommaze.TwoRoomsEnvironment;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorBehavior extends Behavior{
	
	protected TwoRoomsEnvironment env;
	protected double distanceToDoor = 0.2;
	protected boolean openedDoor = false;
	protected Robot robot;
	protected int stopTime = 0;

	public OpenDoorBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		env = (TwoRoomsEnvironment)simulator.getEnvironment();
		distanceToDoor = args.getArgumentAsDoubleOrSetDefault("distancetodoor", distanceToDoor);
		stopTime = args.getArgumentAsIntOrSetDefault("stoptime", stopTime);
		this.robot = r;
	}

	@Override
	public void controlStep(double time) {
		openedDoor = false;
		LinkedList<Wall> buttons = env.getButtons();
		Vector2d r = robot.getPosition(); 
		
		for(Wall b : buttons) {
			Vector2d bPos = b.getPosition();
			
			if((bPos.getX() > 0 && r.getX() > bPos.getX()) || bPos.getX() < 0 && r.getX() < bPos.getX()) {
				if(bPos.distanceTo(r) < distanceToDoor){
					openedDoor = env.openDoor(bPos.getX());
					break;
				}
			}
		}
		((DifferentialDriveRobot) robot).stopTimestep(stopTime);
	}
}