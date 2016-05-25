package actuators;

import java.util.LinkedList;

import mathutils.Vector2d;
import roommaze.TwoRoomsEnvironment;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class StopActuator extends Actuator {
	
	private double activation = 0;

	public StopActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
	}

	@Override
	public void apply(Robot robot,double timeDelta) {
		if(activation > 0.5) {
			((DifferentialDriveRobot) robot).stopTimestep(1);
		}
	}

	public void setActivation(double value) {
		this.activation = value;
	}

}
