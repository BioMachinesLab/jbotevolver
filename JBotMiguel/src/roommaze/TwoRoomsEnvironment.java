package roommaze;

import java.util.LinkedList;
import java.util.Random;

import epuck.EpuckIRSensor;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class TwoRoomsEnvironment extends Environment {
	
	protected LinkedList<Wall> buttons = new LinkedList<Wall>();
	protected LinkedList<Wall> doors = new LinkedList<Wall>();
	protected int numberOfPreys = 0;
	protected Random preyRandom;
	protected Random random;
	protected double splitRatio = 0.7;
	protected int timeBetweenPreys = 100;
	protected int maxPreys = 10;
	protected int numberOfPicks = 0;
	protected boolean teleport = true;
	public boolean doorsOpen = false;
	protected int doorTime = 0;
	protected int doorOpenTime = 400;
	protected boolean useDoors = true;
	protected boolean bothRooms = true;
	
	protected double exitWidth = 0.15;
	protected double arenaWidth = 0;
	protected double arenaHeight = 0;
	protected double corridorWidth = 0.4;
	
	protected boolean teleportOpenDoor = false;
	protected boolean removeWalls = false;
	protected double randomizeOrientation = 0;
	
	protected boolean postEval = false;
	protected int extraPostEvalTime = 0;
	protected int postEvalTime = 1;
	
	protected int[] postEvalPreyRate = new int[2];
	protected boolean postEvalLeftRoom = true;
	protected int postEvalPreyCount = 0;
	protected int startingRoom = 0;
	
	protected boolean allPrey = false;
	
	protected Simulator simulator;
	
	protected Prey[] preys;
	
	private boolean realButton = false;
	protected boolean allowMultipleOpening = false;

	public TwoRoomsEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator,arguments);
		this.simulator = simulator;
		this.random = simulator.getRandom();
		preyRandom = new Random(random.nextLong());
		splitRatio = arguments.getArgumentAsDoubleOrSetDefault("splitratio",splitRatio);
		timeBetweenPreys = arguments.getArgumentAsIntOrSetDefault("timebetweenpreys",timeBetweenPreys);
		maxPreys = arguments.getArgumentAsIntOrSetDefault("maxpreys",maxPreys);
		teleport = arguments.getArgumentAsIntOrSetDefault("teleport",1) == 1;
		doorOpenTime = arguments.getArgumentAsIntOrSetDefault("dooropentime", doorOpenTime);
		useDoors = arguments.getArgumentAsIntOrSetDefault("usedoors",1) == 1;
		exitWidth = arguments.getArgumentAsDoubleOrSetDefault("exitwidth",exitWidth);
		arenaWidth = arguments.getArgumentAsDoubleOrSetDefault("arenawidth",arenaWidth);
		arenaHeight = arguments.getArgumentAsDoubleOrSetDefault("arenaheight",arenaWidth);
		startingRoom = arguments.getArgumentAsIntOrSetDefault("startingroom",startingRoom);
		bothRooms = arguments.getArgumentAsIntOrSetDefault("bothrooms", 1) == 1;
		teleportOpenDoor = arguments.getArgumentAsIntOrSetDefault("teleportopendoor", 0) == 1;
		removeWalls = arguments.getArgumentAsIntOrSetDefault("removewalls", 0) == 1;
		randomizeOrientation = Math.toRadians(arguments.getArgumentAsIntOrSetDefault("randomizeorientation", 0));
		corridorWidth = arguments.getArgumentAsDoubleOrSetDefault("corridorwidth",corridorWidth+random.nextDouble()*0.2);
		
		postEval = arguments.getArgumentAsIntOrSetDefault("posteval", 0) == 1;
		postEvalTime = arguments.getArgumentAsIntOrSetDefault("postevaltime", 0);
		
		realButton = arguments.getArgumentAsIntOrSetDefault("realbutton", 0) == 1;
		
		allowMultipleOpening = arguments.getArgumentAsIntOrSetDefault("allowmultipleopening", 0) == 1;
		allPrey = arguments.getArgumentAsIntOrSetDefault("allprey", 0) == 1; 
		
		if(postEval)
			calculateDropRate(arguments.getArgumentAsDoubleOrSetDefault("postevaldroprate", 0));
		
		preys = new Prey[maxPreys];
		
		if(arguments.getArgumentAsIntOrSetDefault("randomizesplitratio",1) > 0)
			splitRatio = random.nextDouble();
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		createRoom(simulator);	
		for(int i = 0 ; i < preys.length ; i++) {
			preys[i] = new Prey(simulator, "p", new Vector2d(0,-3), 0, 1, 0.03);
			addMovableObject(preys[i]);
			
			if(allPrey) {
				double sign = 1;
				if(random.nextDouble() > 0.5 && bothRooms)
					sign*=-1;
				
				if(startingRoom != 0)
					sign = startingRoom;
				addPrey((preyRandom.nextDouble()*(arenaWidth-0.2-0.2)+corridorWidth/2+0.2)*sign,(preyRandom.nextDouble()*(arenaHeight-0.2-0.2)-arenaHeight/2+0.2));
			}
		}
		
		placeRobots();
	}
	
	protected void placeRobots() {
		
		if(robots.isEmpty())
			return;
		
		if(teleport) {
			double sign = 1;
			if(random.nextDouble() > 0.5 && bothRooms)
				sign*=-1;
			
			if(startingRoom != 0)
				sign = startingRoom;
			
			double randX = random.nextDouble();
			double randY = random.nextDouble();

			robots.get(0).teleportTo(new Vector2d((randX*(arenaWidth-0.2)+corridorWidth/2+0.1)*sign,randY*(arenaHeight-0.2-0.1)-arenaHeight/2+0.1));
			
			double orientation = robots.get(0).getOrientation()+(random.nextDouble()*2-1)*this.randomizeOrientation;
			robots.get(0).setOrientation(orientation);
		}	
	}
	
	public int getNumberOfPicks() {
		return numberOfPicks;
	}
	
	@Override
	public void update(double time) {
		
		if(postEval) {
			if(simulator.getTime()+extraPostEvalTime > postEvalTime)
				simulator.stopSimulation();
		}
		
		if(numberOfPreys < maxPreys) {
			if(time % timeBetweenPreys == 0) {
				double sign;
				
				if(postEval) {
					sign = calculatePostEvalPreyDropRoom();
				} else {
					sign = 1;
					if(preyRandom.nextDouble() > splitRatio)
						sign*=-1;
				}
				
				addPrey((preyRandom.nextDouble()*(arenaWidth-0.2-0.2)+corridorWidth/2+0.2)*sign,(preyRandom.nextDouble()*(arenaHeight-0.2-0.2)-arenaHeight/2+0.2));
			}
		}
		
		Robot r = robots.get(0);
		
		PreyPickerActuator actuator = (PreyPickerActuator)r.getActuatorByType(PreyPickerActuator.class);
		if (actuator != null && actuator.isCarryingPrey()){
			numberOfPicks++;
			Prey preyToDrop = actuator.dropPrey();
			preyToDrop.teleportTo(new Vector2d(0,-3));
			numberOfPreys--;
		}
		
		if(doorsOpen) {
			doorTime++;
			if(doorTime > doorOpenTime)
				closeDoor();
		}
		
	}
	
	protected void addPrey(double x, double y) {
		for(Prey p : preys) {
			if(p.getPosition().getX() == 0 && p.getPosition().getY() == -3) {
				p.setPosition(x,y);
				break;
			}
		}
		numberOfPreys++;
	}
	
	public boolean openDoor(double x) {
		if(!doorsOpen || allowMultipleOpening) {
			
			Robot r = robots.get(0);
			
			if(postEval) {
				EpuckIRSensor sensor = (EpuckIRSensor)r.getSensorByType(EpuckIRSensor.class);

				if(sensor.getSensorReading(2) > 0 || sensor.getSensorReading(1) > 0) {
					extraPostEvalTime+=325;
				}else {
					extraPostEvalTime+=284;
				}
			}
			
			doorTime = 0;
			doorsOpen = true;
			for(Wall w : doors) {
				w.setPosition(w.getPosition().x-20, w.getPosition().y-20);
				w.moveWall();
			}
			
			if(teleportOpenDoor) {
				r = robots.get(0);
				double orientation = r.getOrientation();
			
				Wall w = findClosestWallButton(buttons,r);
				double oldY = w.getPosition().getY();
				double oldX = w.getPosition().getX();
				
				if(oldX < 0) {
					oldX-=0.10;
					orientation = Math.PI/2;
				} else {
					oldX+=0.10;
					orientation = -Math.PI/2;
				}
				r.teleportTo(new Vector2d(oldX,oldY));
				r.setOrientation(orientation);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void closeDoor() {
		if(doorsOpen) {
			doorsOpen = false;
			for(Wall w : doors) {
				w.setPosition(w.getPosition().x+20, w.getPosition().y+20);
				w.moveWall();
			}
		}
	}
	
	protected Wall createWall(Simulator simulator, double x, double y, double width, double height, PhysicalObjectType type) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,type);
		if(!removeWalls || type.equals(PhysicalObjectType.WALLBUTTON))
			addObject(w);
		return w;
	}

	public LinkedList<Wall> getButtons() {
		return buttons;
	}
	
	protected void createRoom(Simulator simulator) {
		
		if(arenaWidth == 0)
			arenaWidth = random.nextDouble()*0.4 + 0.8;
		if(arenaHeight == 0)
			arenaHeight = random.nextDouble()*0.4 + 0.8;
		
		double centerX = -corridorWidth/2 - arenaWidth/2;
		
		//left
		createWall(simulator,centerX-arenaWidth/2,0,0.1,arenaHeight+0.1,PhysicalObjectType.WALL);
		createWall(simulator,centerX,arenaHeight/2,arenaWidth,0.1,PhysicalObjectType.WALL);
		createWall(simulator,centerX,-arenaHeight/2,arenaWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		createWall(simulator,centerX+arenaWidth/2,-arenaHeight/3,0.1,(arenaHeight-exitWidth-0.2)/2+0.1,PhysicalObjectType.WALL);
		createWall(simulator,centerX+arenaWidth/2,arenaHeight/3,0.1,(arenaHeight-exitWidth-0.2)/2+0.1,PhysicalObjectType.WALL);
		
		//right
		createWall(simulator,-centerX+arenaWidth/2,0,0.1,arenaHeight+0.1,PhysicalObjectType.WALL);
		createWall(simulator,-centerX,arenaHeight/2,arenaWidth,0.1,PhysicalObjectType.WALL);
		createWall(simulator,-centerX,-arenaHeight/2,arenaWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		createWall(simulator,-centerX-arenaWidth/2,-arenaHeight/3,0.1,(arenaHeight-exitWidth-0.2)/2+0.1,PhysicalObjectType.WALL);
		createWall(simulator,-centerX-arenaWidth/2,arenaHeight/3,0.1,(arenaHeight-exitWidth-0.2)/2+0.1,PhysicalObjectType.WALL);
		
		//center
		createWall(simulator,0,exitWidth,corridorWidth+0.1,0.1,PhysicalObjectType.WALL);
		createWall(simulator,0,-exitWidth,corridorWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		//buttons
		if(useDoors) {
			doors.add(createWall(simulator,-corridorWidth/2,0,0.1,exitWidth+0.05,PhysicalObjectType.WALL));
			doors.add(createWall(simulator,corridorWidth/2,0,0.1,exitWidth+0.05,PhysicalObjectType.WALL));
		}
		
		buttons.add(createWall(simulator,-corridorWidth/2,-0.25,0.1,0.1,PhysicalObjectType.WALLBUTTON));
		buttons.add(createWall(simulator,corridorWidth/2,0.25,0.1,0.1,PhysicalObjectType.WALLBUTTON));
	}
	
	private void  calculateDropRate(double percentage) {
		
		boolean switched = false;
		
		if(percentage > 0.5) {
			switched = true;
			percentage = Math.round((1-percentage)*100.0)/100.0;
		}
		
		double temp = 1.0/percentage;
		
		int first = 0;
		int second = 0;
		
		if(temp != (double)((int)temp)) {
			if(percentage != 0 && percentage != 1)
				throw new NullPointerException();
			first = switched ? 1 : 0;
			second = switched ? 0 : 1;
		}else {
			first = (int)(temp*percentage);
			second = (int)(temp-first);
			if(switched) {
				int t = first;
				first = second;
				second = t;
			}
		}
		postEvalPreyRate =  new int[]{first,second};
	}
	
	protected int calculatePostEvalPreyDropRoom() {
		
		if(postEvalPreyCount == 0) {
			postEvalLeftRoom = !postEvalLeftRoom;
			postEvalPreyCount = postEvalPreyRate[postEvalLeftRoom ? 0 : 1];
		}
		
		if(postEvalPreyCount == 0)
			return calculatePostEvalPreyDropRoom();
		
		postEvalPreyCount--;
		
		return postEvalLeftRoom ? -1 : 1;
	}
	
	protected Wall findClosestWallButton(LinkedList<Wall> buttons, Robot robot) {
		
		int minIndex = 0;
		double x = robot.getPosition().getX();
		
		for(int i = 1 ; i < buttons.size() ; i++) {
			Wall w = buttons.get(i);
			double wx = w.getPosition().getX();
			if(Math.abs(x-wx) < Math.abs(x-buttons.get(minIndex).getPosition().getX()))
				minIndex = i;
		}
		return buttons.get(minIndex);
	}
}