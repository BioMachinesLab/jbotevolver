package environments;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;

public class LightEnvironment extends Environment{
	
	private LightPole lightpole;
	private Simulator simulator;
	
	public LightEnvironment(Simulator simulator, Arguments args) {
		super(simulator,args);
		this.simulator = simulator;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		lightpole = new LightPole(simulator, "light", 1, 1, 0.5);
		addObject(lightpole);
	}

	@Override
	public void update(double time) {
		
	}
	
	public void moveLightpole(double x, double y) {
		lightpole.setPosition(x,y);
	}
}