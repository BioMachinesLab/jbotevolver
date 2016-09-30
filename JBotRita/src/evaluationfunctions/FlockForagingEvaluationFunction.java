package evaluationfunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;
import robots.JumpingSumo;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import enviromentsSwarms.EmptyEnviromentsWithFixPositions;
import enviromentsSwarms.FireEnv;
import enviromentsSwarms.IntensityForagingRobotsEnviroments;
import environments_JumpingSumoIntensityPreys2.copy.JS_EasiestEnv_JustPreys2;

public class FlockForagingEvaluationFunction extends JumpingSumoEvaluationFunction {

	private double current = 0.0;
	private double clusters = 0.0;
	private HashMap<Robot, Vector2d> robotsPosition = new HashMap<Robot, Vector2d>();
	private double sumOfDistances = 0.0;
	private ArrayList<Robot> robots = new ArrayList<Robot>();
	private double rewardForOrientation=0.0;
	private double rewardForCohesion=0.0;
	private Simulator simulator;
	private double distance=0.0;
	private ArrayList<Boolean> environmentPositions=new ArrayList<Boolean>();
	

	public FlockForagingEvaluationFunction(Arguments args) {
		super(args);
	}

	public double getFitness() {
		return fitness;
	}

	
	// @Override
	public void update(Simulator simulator) {
		
		IntensityForagingRobotsEnviroments environment = (IntensityForagingRobotsEnviroments) simulator
				.getEnvironment();
		robots = environment.getRobots();
		double numberOfRobots = robots.size();
		for(Robot r: robots){
			if (r.isInvolvedInCollisonWall()) {
				current++;
			}
		}
		//System.out.println("fitness"+fitness);
		fitness = environment.getNumberOfFoodSuccessfullyForaged()/numberOfRobots;
		if(fitness==1)
			fitness+= (1 - simulator.getTime() / environment.getSteps());
			//System.out.println(fitness);
	}

	

}