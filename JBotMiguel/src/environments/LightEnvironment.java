package environments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
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

}






/*
 
if(robots.get(0).getPosition().distanceTo(lightpole.getPosition()) < 0.5) {
			double x = simulator.getRandom().nextDouble()*width-width/2;
			double y = simulator.getRandom().nextDouble()*height-height/2;
			lightpole.setPosition(new Vector2d(x,y));
		}



Wall wLeft = new Wall(simulator,"wall",-width/2,0,0,0,0,0,0.1,width,PhysicalObjectType.WALL);
		Wall wRight = new Wall(simulator,"wall1",width/2,0,0,0,0,0,0.1,width,PhysicalObjectType.WALL);
		Wall wTop = new Wall(simulator,"wall2",0,width/2,0,0,0,0,width,0.1,PhysicalObjectType.WALL);
		Wall wBottom = new Wall(simulator,"wall3",0,-width/2,0,0,0,0,width,0.1,PhysicalObjectType.WALL);
		addObject(wLeft);
		addObject(wRight);
		addObject(wTop);
		addObject(wBottom);

*/