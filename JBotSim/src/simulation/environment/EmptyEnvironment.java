package simulation.environment;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class EmptyEnvironment extends Environment {

	private double forageLimit;
	private double forbiddenArea;
	private double distance;
	
	public EmptyEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		distance = arguments.getArgumentAsDoubleOrSetDefault("distance", distance);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		if(distance > 0) {
			for(Robot r : robots) {
				double x = simulator.getRandom().nextDouble()*distance*2-distance;
				double y = simulator.getRandom().nextDouble()*distance*2-distance;
				r.setPosition(x, y);
				r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			}
		}
		
	}
	
	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	
	@Override
	public void update(double time) {}
}