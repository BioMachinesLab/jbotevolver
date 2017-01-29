package enviromentsSwarms;

import java.awt.Color;

import collisionHandling.JumpingZCollisionManager;
import mathutils.Vector2d;
import net.jafama.FastMath;
import physicalobjects.IntensityPrey;
import physicalobjects.WallWithZ;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import environments_ForagingIntensityPreys.ForagingIntensityPreysEnvironment;

public class IntensityForagingRobotsEnviroments extends ForagingIntensityPreysEnvironment {

	@ArgumentsAnnotation(name="preyIntensity", defaultValue="30.0")
	protected double intensity;
	
	protected double limitWidhtWalls=width;
	protected double limitHeightWalls=height;
	protected int generation=0;
	
	
	public IntensityForagingRobotsEnviroments(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		generation = arguments.getArgumentAsInt("generations");
		intensity = arguments.getArgumentIsDefined("preyIntensity") ? arguments
				.getArgumentAsInt("preyIntensity") : 30.0;
		collisionManager = new JumpingZCollisionManager(simulator);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);		
		
		
		for(int i=0; i<numberOfPreys; i++){
			
			
			this.addPrey(new IntensityPrey(simulator, "Prey " + i,randomPosition(), 0, PREY_MASS, 0.1, intensity));
			//this.addPrey(new IntensityPrey(simulator, "Prey " + i,new Vector2d(4,4), 0, PREY_MASS, 0.1, intensity));
			//if(generation > 70){
				//this.addPrey(new IntensityPrey(simulator, "Prey " + i,new Vector2d(4,4), 0, PREY_MASS, 0.1, intensity));
			//}
			//System.out.println(simulator.getEnvironment().getPrey());
		}
		for(Robot r : robots) {
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
		addWalls();
	}

	protected void addWalls(){
		addStaticObject(new WallWithZ(simulator, 0, limitWidhtWalls/2, limitWidhtWalls, 0.08, 10)); // HorizontalWallNorth
		addStaticObject(new WallWithZ(simulator, limitHeightWalls/2, 0, 0.08, limitHeightWalls, 10)); // VerticalEast
		addStaticObject(new WallWithZ(simulator, 0, -limitWidhtWalls/2, limitWidhtWalls, 0.08,10)); // HorizontalSouth
		addStaticObject(new WallWithZ(simulator, -limitHeightWalls/2, 0, 0.08, limitHeightWalls,10)); // VerticalWest
	
	
	}
	

	
	
	@Override
	public void update(double time) {

		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			//System.out.println(prey.getIntensity());
			

			numberOfFoodSuccessfullyForaged =(int) (intensity- prey.getIntensity());
			//System.out.println("numberOfFoodSuccessfullyForaged"+numberOfFoodSuccessfullyForaged);
			if (numberOfFoodSuccessfullyForaged==robots.size()){
				prey.setColor(Color.WHITE);
				simulator.stopSimulation();
			}
			if (prey.getIntensity() >0)
				prey.setColor(Color.BLACK);
		}

	}
	
	protected Vector2d randomPosition(){
		return new Vector2d(5,6);
		//return new  Vector2d(random.nextDouble() * (width/2)-width/2,
		//				random.nextDouble() * (height/2)-height/2);
	}
	
}