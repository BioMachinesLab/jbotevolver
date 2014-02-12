package simulation.robot.actuators;

import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
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
	public void apply(Robot robot) {
		if(activation > 0.5) {
			((DifferentialDriveRobot) robot).stopTimestep(1);
		}
	}

	public void setActivation(double value) {
		this.activation = value;
	}

}
