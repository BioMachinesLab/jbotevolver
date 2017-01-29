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

	private double current = 0.0;
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
				current++;
			}
		}
		//System.out.println("fitness"+fitness);
		
			fitness = environment.getNumberOfFoodSuccessfullyForaged()/robots.size()+current*-0.001/robots.size();
			if(fitness==1)
				fitness+= (1 - simulator.getTime() / environment.getSteps());
			
			//System.out.println(fitness);
	}

	

}