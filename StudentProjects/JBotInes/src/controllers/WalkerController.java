package controllers;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class WalkerController extends Controller{

	private Vector2d nest = new Vector2d(0, 0);
	private Random random;
	private int current = 1; // 1 forward / -1 backward 
	private double prob = 0.0;
	private double maxspeed = 1;
	
	public WalkerController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		random = simulator.getRandom();
		if(args.getArgumentIsDefined("actuators")){
			Arguments actuatorArgs = new Arguments(args.getArgumentAsString("actuators"));
			if(actuatorArgs.getArgumentIsDefined("maxspeed")){
					maxspeed = actuatorArgs.getArgumentAsDoubleOrSetDefault("maxspeed", maxspeed);
			}
		}
		
		((DifferentialDriveRobot) robot).setWheelSpeed(current*maxspeed, current*maxspeed);
		
	}

	@Override
	public void controlStep(double time) {
		
		prob += random.nextDouble()/150;
		if(prob >= 1){
			if(current == 1){
				current = -1;
				prob = 0.0;
			}
			else{
				current = 1;
				prob = 0.0;
			}
		}
		((DifferentialDriveRobot) robot).setWheelSpeed(current*maxspeed, current*maxspeed);
	}
}
