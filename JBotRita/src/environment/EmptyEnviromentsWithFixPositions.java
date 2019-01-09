package environment;

import mathutils.Vector2d;
import net.jafama.FastMath;
import physicalobjects.WallWithZ;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class EmptyEnviromentsWithFixPositions extends Environment{

	@ArgumentsAnnotation(name="distance", defaultValue="0.20")	
	protected double distance;
	
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
	
		//distance= FastMath.sqrtQuick(5*robots.size()/Math.PI);
		
		

		for(Robot r : robots) {
			//robot.size= (distance*2^2)*5
			//distance=raiz quadrada(robot.size/5)/2
			
			distance = FastMath.sqrtQuick(robots.size()/5.0)/5.0;
			
			double x = simulator.getRandom().nextDouble()*distance*2-distance;
			double y = simulator.getRandom().nextDouble()*distance*2-distance;
			
			r.setPosition(x, y);
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			
			/*
			double radius = simulator.getRandom().nextDouble() * (5*0.20)*(robots.size()/(double)5);  
			double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
			r.setPosition( new Vector2d(radius * Math.cos(angle), radius
					* Math.sin(angle)));
					*/
			
			
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
	
