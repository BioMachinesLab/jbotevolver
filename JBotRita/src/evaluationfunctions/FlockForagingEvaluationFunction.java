package evaluationfunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import collisionHandling.JumpingZCollisionManager;
import mathutils.Vector2d;
import robots.JumpingSumo;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.collisionhandling.SimpleCollisionManager;
import simulation.robot.Robot;
import simulation.util.Arguments;
import enviromentsSwarms.EmptyEnviromentsWithFixPositions;
import enviromentsSwarms.FireEnv;
import enviromentsSwarms.IntensityForagingRobotsEnviroments;
import environments_JumpingSumoIntensityPreys2.copy.JS_EasiestEnv_JustPreys2;

public class FlockForagingEvaluationFunction extends JumpingSumoEvaluationFunction {

	private double numberCollisions = 0.0;
	private ArrayList<Robot> robots = new ArrayList<Robot>();
	private Simulator simulator;
	private IntensityForagingRobotsEnviroments environment=null;

	public FlockForagingEvaluationFunction(Arguments args) {
		super(args);
		
	}

	public double getFitness() {
		
		return fitness;
	}

	
	// @Override
	public void update(Simulator simulator) {
		
		 environment = (IntensityForagingRobotsEnviroments) simulator
				.getEnvironment();
		robots = environment.getRobots();
		
		for(Robot r: robots){
			if (r.isInvolvedInCollison()) {
				numberCollisions++;
			}
		}
		//System.out.println("fitness"+fitness);
			int max_PossibleNumberOfCollisions=environment.getSteps()*robots.size();
			double penalty_for_collision= -numberCollisions/max_PossibleNumberOfCollisions;
			fitness = environment.getNumberOfFoodSuccessfullyForaged()/robots.size();
			if(fitness==1)
				fitness+= (1 - simulator.getTime() / environment.getSteps());
			fitness+=penalty_for_collision;
			
		
	}

	/*
	 * fitness = environment.getNumberOfFoodSuccessfullyForaged()/robots.size();
			if(fitness==1)
				fitness+= (1 - simulator.getTime() / environment.getSteps()) +penalty_for_collision*0.2;
			else{
				fitness+= penalty_for_collision*0.1;
			}
	 */
	

}