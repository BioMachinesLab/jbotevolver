package evaluationfunctions.flocking;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.IntensityFlockingNavigationRobotsEnviroments;
import environment.IntensityForagingRobotsEnviroments;
import evaluationfunctions.ForagingWithJumpingSumo;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

/**Same as the ForagingFlock fitness function, but with different bootstraping component:
 * 		- the fitness function has a value in [0, 1] depending on how close the swarm is to the food patch. 
 * @author Rita Ramos
 */

public class ForagingFlockWithBootstraping extends ForagingFlock {
	
	protected Vector2d nest = new Vector2d(0, 0);
	protected double bootstrapingComponentCloserToPrey = 0.0;

	public ForagingFlockWithBootstraping(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		this.simulator=simulator;
		environment = (IntensityForagingRobotsEnviroments) simulator
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
	
	@Override
	protected double boostrapingComponent(){
		return bootstrapingComponentCloserToPrey; 
	}
	
	


}