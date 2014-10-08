package environment;

import java.util.Random;

import mathutils.Vector2d;
import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.environment.TMazeEnvironment;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class WeirdMazeEnvironment extends TMazeEnvironment {

	private Random random;
	private LightPole light;
	private Arguments args;
	
	public WeirdMazeEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments,true);
		this.random = simulator.getRandom();
		args = arguments;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		applyOffset(-2.5, 0);
		
		int sample = args.getArgumentAsInt("fitnesssample");
		int numberOfRobots;
		
		if(sample % 2 == 0){
			numberOfRobots = 20;
		}else{
			numberOfRobots = 15;
		}
		
		for (Robot robot : getRobots()) {
			robot.setPosition(newRandomPosition(true));
			robot.setOrientation(random.nextDouble()*(2*Math.PI));
		}
		
		changeNumberOfRobots(numberOfRobots);
		
		light = new LightPole(simulator, "lightpole", 3.8, 0, 0.2);
		
		addStaticObject(light);
		
	}
	
	@Override
	public void update(double time) {
		
	}
	
	private Vector2d newRandomPosition(boolean turnRight) {
		double x = random.nextDouble() * (0.5) - (5.1/2);
		double y = random.nextDouble() * (0.5) - (0.9/3);
		return new Vector2d(x, y);
	}
	
	public LightPole getLightPole() {
		return light;
	}
	 
	/**
	 * 
	 * Robots that have an id bigger or equal to the 'number' received are disable and place far away from the arena.
	 * 
	 * @param number
	 */
	private void changeNumberOfRobots(double number) {
		for (Robot r : getRobots()) {
			if(r.getId() >= number){
				r.setPosition(1000, 1000);
				r.setEnabled(false);
			}
		}
	}
	
}
