package evaluationfunctions;

import java.util.ArrayList;

import robot.Thymio;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.entities.Entity;
import commoninterface.entities.PreyEntity;

import environment.CameraTrackerEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ThymioForageEvaluationFunction extends EvaluationFunction {

	private static final double FORAGE_DISTANCE = 1;
	int foodForaged = 0;
	private double averageWheelSpeed, timesteps;

	public ThymioForageEvaluationFunction(Arguments args) {
		super(args);
		
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getEnvironment().getRobots();
		
		timesteps = simulator.getTime();
		foodForaged = ((CameraTrackerEnvironment)simulator.getEnvironment()).getPreysCaught();
		
		for (Robot robot : robots) {
			Thymio thymio = (Thymio)robot;
			double closestPreyDistanceToRobot = Double.MAX_VALUE;			
			
			for(Entity entity : thymio.getEntities()){
				if(entity instanceof PreyEntity){
					double distanceToRobot = ((PreyEntity)entity).getPosition().distanceTo(thymio.getVirtualPosition());
					if(distanceToRobot < closestPreyDistanceToRobot)
						closestPreyDistanceToRobot = distanceToRobot;
				}
			}

			if(closestPreyDistanceToRobot < Double.MAX_VALUE && closestPreyDistanceToRobot <= FORAGE_DISTANCE)
				fitness += ((FORAGE_DISTANCE - closestPreyDistanceToRobot) / FORAGE_DISTANCE)/simulator.getEnvironment().getSteps()*0.0001;
			
			averageWheelSpeed += ((thymio.getLeftWheelSpeed() + thymio.getRightWheelSpeed())/2)/robots.size();
			
			if(robot.isInvolvedInCollison()){
				fitness = -1;
				foodForaged = 0;
				simulator.stopSimulation();
			}
			
		}
		
	}

	@Override
	public double getFitness() {
		if (averageWheelSpeed/timesteps < 0)
			return -1;
		else
			return super.getFitness() +  foodForaged;
	}
	
}
