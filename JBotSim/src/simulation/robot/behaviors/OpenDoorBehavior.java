package simulation.robot.behaviors;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorBehavior extends Behavior{
	
	private TwoRoomsEnvironment env;
	private double distanceToDoor = 0.2;

	public OpenDoorBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		env = (TwoRoomsEnvironment)simulator.getEnvironment();
		distanceToDoor = args.getArgumentAsDoubleOrSetDefault("distancetodoor", distanceToDoor);
	}

	@Override
	public void controlStep(double time) {
		LinkedList<Wall> buttons = env.getButtons();
		Robot rob = env.getRobots().get(0);
		Vector2d r = rob.getPosition(); 
		
		for(Wall b : buttons) {
			Vector2d bPos = b.getPosition();
			if(bPos.distanceTo(r) < distanceToDoor) {
				env.openDoor();
				break;
			}
		}
		rob.setWheelSpeed(0, 0);
	}
}