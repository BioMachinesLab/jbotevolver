package environment;

import physicalobjects.WallWithZ;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class EmptyEnviromentsWithFixPositions extends Environment{

	@ArgumentsAnnotation(name="distance", defaultValue="0.20")	
	private double distance;
	
	@ArgumentsAnnotation(name = "putWalls", values={"0","1"})
	protected boolean putWalls;
	
	protected double wallsSafeMargin = 0.2;
	protected double wallsDensity = 0.08;
	
	protected Simulator simulator;


	public EmptyEnviromentsWithFixPositions(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		distance = arguments.getArgumentIsDefined("distance") ? arguments
				.getArgumentAsInt("distance") : 0.20;
		putWalls   = arguments.getArgumentIsDefined("putWalls") ? (arguments.getArgumentAsInt("putWalls")==1)	: false;
		this.simulator = simulator;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
			for(Robot r : robots) {
				double x = simulator.getRandom().nextDouble()*distance*2-distance;
				double y = simulator.getRandom().nextDouble()*distance*2-distance;
				r.setPosition(x, y);
				r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			}
		if(putWalls)
			addWalls();
	}
	
	protected void addWalls() {
		addStaticObject(new WallWithZ(simulator, 0, width / 2,
				width, wallsDensity, 10)); // HorizontalWallNorth
		addStaticObject(new WallWithZ(simulator, height / 2, 0,
				wallsDensity, height, 10)); // VerticalEast
		addStaticObject(new WallWithZ(simulator, 0, -width / 2,
				width, wallsDensity, 10)); // HorizontalSouth
		addStaticObject(new WallWithZ(simulator, -height / 2, 0,
				wallsDensity, height, 10)); // VerticalWest
	}

	@Override
	public void update(double time) {}
	
}
	
