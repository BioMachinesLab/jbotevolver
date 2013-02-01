package simulation.environment;

import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TMazeEnvironment.Square;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoRoomsEnvironment extends Environment {
	
	private LinkedList<Wall> buttons = new LinkedList<Wall>();
	private int numberOfPreys = 0;
	private Random r = new Random();
	private double splitRatio = 0.7;
	private int timeBetweenPreys = 100;
	private int maxPreys = 10;
	private int numberOfPicks = 0;
	private boolean teleported = false;
	private boolean teleport = true;
	public boolean doorsOpen = false;
	private int doorTime = 0;
	private int doorOpenTime = 200;
	private boolean useDoors = true;
	private boolean bothRooms = true;
	
	private double exitWidth = 0.15;
	private double arenaWidth = 0;
	private double corridorWidth = 1;
	
	private boolean teleportOpenDoor = false;
	private boolean removeWalls = false;

	public TwoRoomsEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator,arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 7, 
				  arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 7);
		
		r.setSeed(simulator.getRandom().nextLong());
		splitRatio = arguments.getArgumentAsDoubleOrSetDefault("splitratio",splitRatio);
		timeBetweenPreys = arguments.getArgumentAsIntOrSetDefault("timebetweenpreys",timeBetweenPreys);
		maxPreys = arguments.getArgumentAsIntOrSetDefault("maxpreys",maxPreys);
		teleport = arguments.getArgumentIsDefined("teleport") ? arguments.getArgumentAsInt("teleport") == 1 : teleport;
		doorOpenTime = arguments.getArgumentAsIntOrSetDefault("dooropentime", doorOpenTime);
		useDoors = arguments.getArgumentIsDefined("usedoors") ? arguments.getArgumentAsInt("usedoors") == 1 : useDoors;
		exitWidth = arguments.getArgumentAsDoubleOrSetDefault("exitwidth",exitWidth);
		arenaWidth = arguments.getArgumentAsDoubleOrSetDefault("arenawidth",arenaWidth);
		bothRooms = arguments.getArgumentAsIntOrSetDefault("bothrooms", 1) == 1;
		teleportOpenDoor = arguments.getArgumentAsIntOrSetDefault("teleportopendoor", 0) == 1;
		removeWalls = arguments.getArgumentAsIntOrSetDefault("removewalls", 0) == 1;
		
		if(arguments.getArgumentAsIntOrSetDefault("randomizesplitratio",1) > 0)
			splitRatio = r.nextDouble();
		
		createRoom();
	}
	
	public int getNumberOfPicks() {
		return numberOfPicks;
	}
	
	@Override
	public void update(double time) {
		
		if(!teleported && teleport) {
			double sign = 1;
			if(r.nextDouble() > 0.5 && bothRooms)
				sign*=-1;

			double ySign = r.nextDouble() > 0.5 ? -1 : 1;
			
			robots.get(0).teleportTo(new Vector2d((r.nextDouble()*arenaWidth*0.66+corridorWidth/1.5)*sign,ySign*arenaWidth/2.0*r.nextDouble()*0.66));
			teleported = true;
		}
		
		if(numberOfPreys < maxPreys) {
			if(time % timeBetweenPreys == 0) {
				double sign = 1;
				if(r.nextDouble() > splitRatio)
					sign*=-1;
				//preys between x={2,5}*squareSize ; y={-1,1}*squareSize
				addPrey((r.nextDouble()*arenaWidth*0.66+corridorWidth/1.5)*sign,(r.nextDouble()*arenaWidth*0.66-arenaWidth/3));
			}
		}
		
		Robot r = simulator.getEnvironment().getRobots().get(0);
		if(r.isCarryingPrey()) {
			numberOfPicks++;
			r.dropPrey().teleportTo(new Vector2d(0,-3));
			numberOfPreys--;
		}
		
		if(doorsOpen) {
			doorTime++;
			if(doorTime > doorOpenTime)
				closeDoor();
		}
		
	}
	
	private void addPrey(double x, double y) {
		Vector2d pos = new Vector2d(x, y);
		addPrey(new Prey(simulator, "p", pos, 0, 1, 0.03));
		numberOfPreys++;
	}
	
	public void openDoor() {
		if(!doorsOpen) {
			doorTime = 0;
			doorsOpen = true;
			for(Wall w : buttons) {
				w.setPosition(w.getPosition().x-20, w.getPosition().y-20);
				w.moveWall();
			}
			
			if(teleportOpenDoor) {
				Robot r = robots.get(0);
				double orientation = r.getOrientation();
				double oldX = r.getPosition().getX();
				double oldY = r.getPosition().getY();
				
				if(oldX < 0) {
					oldY = 0.2;
					orientation = 0;
				} else {
					oldY = -0.2;
					orientation = Math.PI;
				}
				
				r.teleportTo(new Vector2d(oldX,oldY));
				r.setOrientation(orientation);
				
			}
		}
	}
	
	public void closeDoor() {
		if(doorsOpen) {
			doorsOpen = false;
			for(Wall w : buttons) {
				w.setPosition(w.getPosition().x+20, w.getPosition().y+20);
				w.moveWall();
			}
		}
	}
	
	protected Wall createWall(double x, double y, double width, double height, PhysicalObjectType type) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,type);
		if(!removeWalls || type.equals(PhysicalObjectType.WALLBUTTON))
			addObject(w);
		return w;
	}

	public LinkedList<Wall> getButtons() {
		return buttons;
	}
	
	protected void createRoom() {
		
		if(arenaWidth == 0)
			arenaWidth = simulator.getRandom().nextDouble()/2 + 1;
		
		double centerX = -corridorWidth/2 - arenaWidth/2;
		
		//left
		createWall(centerX-arenaWidth/2,0,0.1,arenaWidth+0.1,PhysicalObjectType.WALL);
		createWall(centerX,arenaWidth/2,arenaWidth,0.1,PhysicalObjectType.WALL);
		createWall(centerX,-arenaWidth/2,arenaWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		createWall(centerX+arenaWidth/2,-arenaWidth/3,0.1,arenaWidth/3+0.1,PhysicalObjectType.WALL);
		createWall(centerX+arenaWidth/2,arenaWidth/3,0.1,arenaWidth/3+0.1,PhysicalObjectType.WALL);
		
		//right
		createWall(-centerX+arenaWidth/2,0,0.1,arenaWidth+0.1,PhysicalObjectType.WALL);
		createWall(-centerX,arenaWidth/2,arenaWidth,0.1,PhysicalObjectType.WALL);
		createWall(-centerX,-arenaWidth/2,arenaWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		createWall(-centerX-arenaWidth/2,-arenaWidth/3,0.1,arenaWidth/3+0.1,PhysicalObjectType.WALL);
		createWall(-centerX-arenaWidth/2,arenaWidth/3,0.1,arenaWidth/3+0.1,PhysicalObjectType.WALL);
		
		//center
		createWall(0,exitWidth,1.1,0.1,PhysicalObjectType.WALL);
		createWall(0,-exitWidth,1.1,0.1,PhysicalObjectType.WALL);
		
		if(useDoors) {
			//buttons
			buttons.add(createWall(-corridorWidth/2,0,0.1,exitWidth+0.05,PhysicalObjectType.WALLBUTTON));
			buttons.add(createWall(corridorWidth/2,0,0.1,exitWidth+0.05,PhysicalObjectType.WALLBUTTON));
		}
	}
	
}
