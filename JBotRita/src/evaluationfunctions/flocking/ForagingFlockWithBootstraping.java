package evaluationfunctions.flocking;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.IntensityFlockingNavigationRobotsEnviroments;
import evaluationfunctions.ForagingWithJumpingSumo;

public class ForagingFlockWithBootstraping extends
		ForagingWithJumpingSumo {

	private double numberCollisions = 0.0;
	private ArrayList<Robot> robots = new ArrayList<Robot>();
	private Simulator simulator;
	private IntensityFlockingNavigationRobotsEnviroments environment = null;
	private Vector2d nest = new Vector2d(0, 0);
	private double bootstrapingComponentCloserToPrey = 0.0;

	public ForagingFlockWithBootstraping(Arguments args) {
		super(args);

	}

	@Override
	public double getFitness() {
		if (environment.getNumberOfFoodSuccessfullyForaged()==robots.size()) {
			double timeSpent=simulator.getTime();
			double max_PossibleNumberOfCollisions=timeSpent*robots.size();
			double penalty_for_collision= -numberCollisions/max_PossibleNumberOfCollisions;
			return 2 + (1 - simulator.getTime() / environment.getSteps())
					+ penalty_for_collision;
		} else {
			return bootstrapingComponentCloserToPrey;
		}
		
	}

	@Override
	public void update(Simulator simulator) {
		this.simulator=simulator;
		environment = (IntensityFlockingNavigationRobotsEnviroments) simulator
				.getEnvironment();
		robots = environment.getRobots();
		Vector2d preyPosition = environment.getPrey().get(0).getPosition();

		double sum_RobotsGettingCloserToThePrey = 0.0;
		
		for (Robot r : robots) {
			if (r.isInvolvedInCollison()) {
				numberCollisions++;
			}
			double initialDistanceToPrey = preyPosition.distanceTo(nest);
			sum_RobotsGettingCloserToThePrey += (1 - (r.getDistanceBetween(preyPosition) / initialDistanceToPrey));
		}
		
		double avarage_RobotsGettingCloserToThePrey = sum_RobotsGettingCloserToThePrey/robots.size();
		if (avarage_RobotsGettingCloserToThePrey > bootstrapingComponentCloserToPrey)
			bootstrapingComponentCloserToPrey = avarage_RobotsGettingCloserToThePrey;

	}

}