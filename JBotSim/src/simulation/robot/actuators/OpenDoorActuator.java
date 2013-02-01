package simulation.robot.actuators;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorActuator extends Actuator {
	
	private TwoRoomsEnvironment env;
	private double distanceToDoor = 0.2;
	private boolean open = false;
	private Simulator simulator;

	public OpenDoorActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id);
		this.simulator = simulator;
		args.getArgumentAsDoubleOrSetDefault("distancetodoor", distanceToDoor);
	}

	@Override
	public void apply(Robot robot) {
		if(open) {
			if(env == null)
				env = (TwoRoomsEnvironment)simulator.getEnvironment();
			
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
			
			((DifferentialDriveRobot) robot).setWheelSpeed(0, 0);
		}
		open = false;
	}

	public void open(boolean open) {
		this.open = open;		
	}
}
