package evaluationfunctions;

import java.util.ArrayList;

import robot.Thymio;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.objects.Entity;
import commoninterface.objects.PreyEntity;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ThymioForageEvaluationFunction extends EvaluationFunction {

	private static final double FORAGE_DISTANCE = 5;

	public ThymioForageEvaluationFunction(Arguments args) {
		super(args);
		
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getEnvironment().getRobots();
		
		for (Robot robot : robots) {
			Thymio thymio = (Thymio)robot;
			ArrayList<PreyEntity> preys = new ArrayList<PreyEntity>();
			
			for(Entity entity : thymio.getEntities()){
				if(entity instanceof PreyEntity)
					preys.add((PreyEntity)entity);
			}
			
			double closestPreyDistanceToRobot = Double.MAX_VALUE;
			
			for(PreyEntity p : preys) {
				double distanceToRobot = p.getPosition().distanceTo(thymio.getVirtualPosition());
				if(distanceToRobot < closestPreyDistanceToRobot) {
					closestPreyDistanceToRobot = distanceToRobot;
				}
			}
			
			if(closestPreyDistanceToRobot < Double.MAX_VALUE && closestPreyDistanceToRobot <= FORAGE_DISTANCE){
				fitness += (FORAGE_DISTANCE - closestPreyDistanceToRobot) / FORAGE_DISTANCE;
			}
			
		}
		
	}

}
