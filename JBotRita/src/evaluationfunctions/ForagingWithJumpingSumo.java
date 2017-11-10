package evaluationfunctions;

import java.util.ArrayList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

import environments_JumpingSumoIntensityPreys2.copy.JS_Environment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ForagingWithJumpingSumo extends EvaluationFunction {
	private int numberOfFoodForaged = 0;
	private double initialDistance;
	private double current = 0.0;
	private double bootstrapingComponentCloserToPrey = 0;

	public ForagingWithJumpingSumo(Arguments args) {
		super(args);
	}

	@Override
	public double getFitness() {
		if (numberOfFoodForaged < 1) {
			return bootstrapingComponentCloserToPrey + current * -0.001;
		} else {
			return fitness;
		}
	}

	@Override
	public void update(Simulator simulator) {
		JS_Environment environment = ((JS_Environment) (simulator
				.getEnvironment()));

		ArrayList<Prey> preys = environment.getPrey();
		Vector2d preyPosition = preys.get(0).getPosition();
		Robot r=environment.getRobots().get(0);
		if (simulator.getTime() > 0) {

			if (r.isInvolvedInCollisonWall()) {
				current++;
			}

			Vector2d robot_position = r.getPosition();

			double robotGettingCloserToTheClosestPrey = bootstrapingComponentCloserToPrey;
			bootstrapingComponentCloserToPrey = (1 - (robot_position
					.distanceTo(preyPosition) / initialDistance));

			if (bootstrapingComponentCloserToPrey < robotGettingCloserToTheClosestPrey)
				bootstrapingComponentCloserToPrey = robotGettingCloserToTheClosestPrey;

			numberOfFoodForaged = environment
					.getNumberOfFoodSuccessfullyForaged();

			if (numberOfFoodForaged == 1) {
				fitness = current * -0.01 + 1
						+ (1 - simulator.getTime() / environment.getSteps());
			}

		} else { // time == 0
			if (preys.size() > 0){
				initialDistance = preyPosition.distanceTo(r.getPosition());
			}	
		}
	}
}
