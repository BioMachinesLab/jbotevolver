package environment;

import java.awt.Color;
import java.util.ArrayList;

import collisionHandling.JumpingZCollisionManager;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;


public class IntensityFlockingNavigationRobotsEnviroments extends
IntensityForagingRobotsEnviroments {

	private ArrayList<Robot> robots_ThatHaveNotFoundTheWaypoint=robots;

	public IntensityFlockingNavigationRobotsEnviroments(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
		collisionManager = new JumpingZCollisionManager(simulator);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
	}


	@Override
	public void update(double time) {

		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			
			ArrayList<Robot> newListOfRobots=new ArrayList<Robot>();
			
			for(Robot r: robots_ThatHaveNotFoundTheWaypoint){  
				if(r.getDistanceBetween(prey.getPosition())>0.3){ 
					newListOfRobots.add(r);
				}
			}
			robots_ThatHaveNotFoundTheWaypoint=newListOfRobots;
			
			numberOfFoodSuccessfullyForaged = robots.size()-robots_ThatHaveNotFoundTheWaypoint.size();

			if (numberOfFoodSuccessfullyForaged == robots.size()) {
				prey.setColor(Color.WHITE);
				simulator.stopSimulation();
			}
			if (prey.getIntensity() > 0)
				prey.setColor(Color.BLACK);
		}

	}

}