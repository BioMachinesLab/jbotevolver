package environments;

import java.util.Random;

import epuck.Epuck;
import behaviors.WalkInCirclesBehavior;
import mathutils.Vector2d;
import sensors.RobotOrientationDistanceSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class SingleRoomEnvironment extends Environment {

	protected Random random;
	
	@ArgumentsAnnotation(name="teammate", values={"0","1"})
	private boolean teammate;
	
	@ArgumentsAnnotation(name="closerobots", values={"0","1"})
	private boolean closeRobots;
	
	@ArgumentsAnnotation(name="preys", defaultValue="0")
	private int numberOfPreys;
	
	@ArgumentsAnnotation(name="randomizewidth", defaultValue="0.0")
	private double randX;
	
	@ArgumentsAnnotation(name="randomizeheight", defaultValue="0.0")
	private double randY;
	
	public SingleRoomEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.random = simulator.getRandom();
		this.teammate = args.getArgumentAsIntOrSetDefault("teammate", 0) == 1;
		this.closeRobots = args.getArgumentAsIntOrSetDefault("closerobots", 0) == 1;
		this.numberOfPreys = args.getArgumentAsIntOrSetDefault("preys", 0);
		this.randX = args.getArgumentAsDoubleOrSetDefault("randomizewidth", 0);
		this.randY = args.getArgumentAsDoubleOrSetDefault("randomizeheight", 0);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		width+=width*random.nextDouble()*2*randX-randX;
		height+=height*random.nextDouble()*2*randY-randY;
		
		createWall(simulator,width/2,0,0.1,height+0.1,PhysicalObjectType.WALL);
		createWall(simulator,-width/2,0,0.1,height+0.1,PhysicalObjectType.WALL);
		
		createWall(simulator,0,height/2,width+0.1,0.1,PhysicalObjectType.WALL);
		createWall(simulator,0,-height/2,width+0.1,0.1,PhysicalObjectType.WALL);
		
		if(closeRobots) {
			int numberOfRobots = simulator.getRobots().size();
			
			int side = (int)Math.ceil(Math.sqrt(numberOfRobots));
			int line = 0;
			int column = 0;
			
			double randX = (random.nextDouble()*width-width/2)*0.5;
			double randY = (random.nextDouble()*height-height/2)*0.5;
			
			for(int i = 0 ; i < numberOfRobots ; i++) {
				if(i % side == 0) {
					column = 0;
					line++;
				}
				Robot robot = simulator.getRobots().get(i);
				
				double x = randX+column*(0.1+random.nextDouble()*0.1);
				double y = randY+line*(0.1+random.nextDouble()*0.1);
				
				robot.teleportTo(new Vector2d(x,y));
				robot.setOrientation(random.nextDouble()*Math.PI*2);
				
				if(robot.isInvolvedInCollison()) {
					i--;
				}else
					column++;
			}
			
		} else {
			for(Robot robot : simulator.getRobots()) {
				robot.teleportTo(new Vector2d((random.nextDouble()*width-width/2)*0.75,(random.nextDouble()*height-height/2)*0.75));
				robot.setOrientation(random.nextDouble()*Math.PI*2);
			}
		}
		
		if(teammate) {
			
			Robot r = new Epuck(simulator, new Arguments(""));
			
			double rand = random.nextDouble();
			
			if(rand < 0.5) {
				r.setController(new WalkInCirclesBehavior(simulator,r,new Arguments("left=0.1,right=0.085")));
				r.setPosition(new Vector2d(0.5,0));
				r.setOrientation(-Math.PI/2);
			} else {
				r.setController(new WalkInCirclesBehavior(simulator,r,new Arguments("left=0.085,right=0.1")));
				r.setPosition(new Vector2d(-0.5,0));
				r.setOrientation(-Math.PI/2);
			}
			
			for(Robot robot : simulator.getRobots()) {
				RobotOrientationDistanceSensor s = (RobotOrientationDistanceSensor)robot.getSensorByType(RobotOrientationDistanceSensor.class);
				if(s != null)
					s.setTrackingRobotId(r.getId());
			}
			addRobot(r);
		}
		
		if(numberOfPreys > 0) {
	
			for(int i = 0 ; i < numberOfPreys ; i++) {
				double randX = (random.nextDouble()*width-width/2)*0.75;
				double randY = (random.nextDouble()*height-height/2)*0.75;
				Prey p = new Prey(simulator,"prey",new Vector2d(randX,randY),0,1,0.05);
				addPrey(p);
			}
		}
	}

	@Override
	public void update(double time) {
	}
	
	protected Wall createWall(Simulator simulator, double x, double y, double width, double height, PhysicalObjectType type) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,type);
		addObject(w);
		return w;
	}
}