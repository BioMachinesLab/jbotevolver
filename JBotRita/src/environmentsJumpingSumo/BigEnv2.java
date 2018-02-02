
package environmentsJumpingSumo;

import java.awt.Color;

import net.jafama.FastMath;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class BigEnv2 extends JS_Environment {  //RATIO2
	
//	private static final double MAX_WIDTH_LIMIT_FOR_WALL = 16.5;//4
//	private static final double MIN_WIDTH_LIMIT_FOR_WALL =14; //2.6
//	private static final double WIDTH_HEIGHT_WALLSURRONDED = 50;
//	
	private  double MAX_WIDTH_LIMIT_FOR_WALL = 33;//4
	private  double MIN_WIDTH_LIMIT_FOR_WALL =28; //2.6
	private static final double WIDTH_HEIGHT_WALLSURRONDED = 100;
//	private double random1, random2;
	private double ratio;
	private Environment env;
	
	
	public BigEnv2(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.steps = 2500;
		
		
		
	}

	public void setup(Environment env, Simulator simulator, double ratio) { // use this for LoopsOfEnvironmnt
		super.setup(simulator);
		this.env=env;
		this.ratio=ratio;
	//	this.random1=random1;
	//	this.random2=random2;
		init();
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		env=this;
		init();
	}

	public void init() {
		
		env.addStaticObject(new Wall(simulator, 0, WIDTH_HEIGHT_WALLSURRONDED/2, WIDTH_HEIGHT_WALLSURRONDED, 0.4)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, WIDTH_HEIGHT_WALLSURRONDED/2, 0, 0.4, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -WIDTH_HEIGHT_WALLSURRONDED/2, WIDTH_HEIGHT_WALLSURRONDED, 0.4)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -WIDTH_HEIGHT_WALLSURRONDED/2, 0, 0.4, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalWest
		
//ambiente do meio
		MAX_WIDTH_LIMIT_FOR_WALL = 50;//4
		MIN_WIDTH_LIMIT_FOR_WALL = 38.75; //3.1

		
		double b = random.nextDouble()
				* (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;
		
		//System.out.println(b);
		
		double a = 16 * b / 63; // ratio 6
		
		double new_b=b-21.25; //1.7
		double new_a = 16*new_b/63;
		
		
		env.addStaticObject(new Wall(simulator, -(MAX_WIDTH_LIMIT_FOR_WALL-(new_b+MAX_WIDTH_LIMIT_FOR_WALL-0.4)/2),
				a-new_a, new_b+MAX_WIDTH_LIMIT_FOR_WALL -0.4, 0.4)); // centerWall
		
		env.addStaticObject(new Wall(simulator, MAX_WIDTH_LIMIT_FOR_WALL-(new_b+MAX_WIDTH_LIMIT_FOR_WALL-0.4)/2,
				-(a-new_a), new_b+MAX_WIDTH_LIMIT_FOR_WALL -0.4, 0.4)); // centerWall

		
		//ambiente depois
		MAX_WIDTH_LIMIT_FOR_WALL = 33;//4
	    MIN_WIDTH_LIMIT_FOR_WALL =28; //2.6
		
		b = random.nextDouble()
				* (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;
//		double ratio= random.nextDouble()
//				* (random2 - random1)
//				+ random1;
		ratio=2;
		double a_2 = b / FastMath.sqrtQuick(ratio*ratio - 1);
		
		env.addStaticObject(new Wall(simulator, 0, a+a_2, b * 2 - 0.8, 0.4)); //centerWall
	
		env.addPrey(new IntensityPrey(simulator, "Prey " + 0, new Vector2d(0, 2*a_2+a), 0, PREY_MASS, 0.125, 1));
		
		
		
		//antes
		
		double MAX_WIDTH_LIMIT_FOR_WALL = 50;//4
		double MAX_WIDTH_LIMITALLOW_FOR_WALL = 46.25; //3.7
		double MIN_WIDTH_LIMIT_FOR_WALL = 17.5;//1.4
		
	
		double b_3 = random.nextDouble()
				* (MAX_WIDTH_LIMITALLOW_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;
		
		double a_3 = b / FastMath.sqrtQuick(15);  
		
		env.addStaticObject(new Wall(simulator, 0, -a-a_3 , b_3 * 2 - 0.8,0.4)); //centerWall
		
		if ( b_3<38) {
			
		double availableSpaceForNewWall= MAX_WIDTH_LIMIT_FOR_WALL-(b_3-4)-4;
		double newWidth=random.nextDouble()*(availableSpaceForNewWall-0.2)+0.2;
		//double newWidth=8.55;
			env.addStaticObject(new Wall(simulator,  MAX_WIDTH_LIMIT_FOR_WALL -( newWidth) / 2,-a-a_3, newWidth, 0.4)); // rightWall
			env.addStaticObject(new Wall(simulator, -MAX_WIDTH_LIMIT_FOR_WALL+ (newWidth) / 2, -a-a_3, newWidth, 0.4)); // leftWall
			//System.out.println(newWidth);
		}		
			
	
		
		env.getRobots().get(0).setPosition(new Vector2d( 0, -a-2*a_3));

		for(Robot r : env.getRobots()) {
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
		
		height=100;
		width=100;
	}
	
	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				numberOfFoodSuccessfullyForaged = 1;
				simulator.stopSimulation();
			}
			if (prey.getIntensity() <= 0)
				prey.setColor(Color.WHITE);
			else if (prey.getIntensity() < 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}
	}
	
}
