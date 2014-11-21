package simulation.robot;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class AquaticDrone extends DifferentialDriveRobot {

	private double inertiaConstant = 0.05;
	private double accelarationConstant = 0.1;
	private Vector2d velocity = new Vector2d();

	public AquaticDrone(Simulator simulator, Arguments args) {
		super(simulator, args);
	}
	
	@Override
	public void updateActuators(Double time, double timeDelta) {

		if (stopTimestep <= 0) {

			orientation = MathUtils.modPI2(orientation + timeDelta * 0.5 / (distanceBetweenWheels / 2.0) * (rightWheelSpeed - leftWheelSpeed));
			double lengthOfAcc = accelarationConstant * ((leftWheelSpeed + rightWheelSpeed) / 2.0);
			Vector2d accelaration = new Vector2d(lengthOfAcc * FastMath.cosQuick(orientation), lengthOfAcc * FastMath.sinQuick(orientation));
			
			velocity.setX(velocity.getX() * (1 - inertiaConstant));
			velocity.setY(velocity.getY() * (1 - inertiaConstant));
			
			velocity.add(accelaration);
			
			position.set(
					position.getX() + timeDelta * velocity.getX(), 
					position.getY() + timeDelta * velocity.getY());
			
		}

		stopTimestep--;

		for (Actuator actuator : actuators) {
			actuator.apply(this);
		}
	}

}
