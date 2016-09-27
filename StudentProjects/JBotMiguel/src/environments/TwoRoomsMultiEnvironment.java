package environments;

import java.awt.Color;

import mathutils.Vector2d;
import roommaze.TwoRoomsEnvironment;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoRoomsMultiEnvironment extends TwoRoomsEnvironment {
	
	protected double maxSize = 0.4;
	protected double minSize = 0.75;
	protected boolean aggregateRobots = true;
	protected boolean specialDoor = false;
	protected boolean firstDoorClosed = false;
	protected boolean openDoorAuto = false;
	protected boolean autoCloseDoor = false;
	protected boolean minMaxSize = false;
	protected boolean holdDoor = false;
	private boolean robotInsideCorridor = false;
	protected double buttonDistance = 0;
	protected double delayDoor = 0;
	protected double currentDelayDoor = 0;
	
	public TwoRoomsMultiEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator,arguments);
		maxSize = arguments.getArgumentAsDoubleOrSetDefault("maxsize",maxSize);
		aggregateRobots = arguments.getArgumentAsIntOrSetDefault("aggregaterobots", 1) == 1;
		specialDoor = arguments.getArgumentAsIntOrSetDefault("specialdoor", 0) == 1;
		autoCloseDoor = arguments.getArgumentAsIntOrSetDefault("autoclosedoor", 0) == 1;
		openDoorAuto = arguments.getArgumentAsIntOrSetDefault("opendoorauto", 0) == 1;
		minMaxSize = arguments.getArgumentAsIntOrSetDefault("minmaxsize", 0) == 1;
		holdDoor = arguments.getArgumentAsIntOrSetDefault("holddoor", 0) == 1;
		buttonDistance = arguments.getArgumentAsIntOrSetDefault("buttondistance", 0);
		delayDoor = arguments.getArgumentAsDoubleOrSetDefault("delaydoor", 0);
		robotInsideCorridor = arguments.getArgumentAsIntOrSetDefault("robotsinsidecorridor", 0) == 1;
		
		if(delayDoor > 0) {
			delayDoor = simulator.getRandom().nextDouble()*delayDoor;
		}
		
		if(minMaxSize) {
			minSize = arguments.getArgumentAsDoubleOrSetDefault("minsize", minSize);
		}
	}
	
	@Override
	protected void placeRobots() {
		
		int numberOfRobots = simulator.getRobots().size();
		
		if(robotInsideCorridor) {
			
			for(Robot r : simulator.getRobots()) {
			
				double randX = random.nextDouble()*(corridorWidth)-corridorWidth/2;
				double randY = random.nextDouble()*(exitWidth)-exitWidth/2;
				r.teleportTo(new Vector2d(randX*0.5,randY*0.6));
				r.setOrientation(random.nextDouble()*Math.PI*2);
			}
			
		
		}else if(aggregateRobots) {
			int side = (int)Math.ceil(Math.sqrt(numberOfRobots));
			int line = 0;
			int column = 0;
			
			double randX = random.nextDouble()*arenaWidth*0.5-0.1;
			double randY = random.nextDouble()*arenaHeight*0.5-arenaHeight/2+0.1;
			
			if(startingRoom < 0) {
				randX = -randX-corridorWidth-corridorWidth/2;
			} else
				randX+=corridorWidth;
			
			for(int i = 0 ; i < numberOfRobots ; i++) {
				if(i % side == 0) {
					column = 0;
					line++;
				}
				Robot robot = simulator.getRobots().get(i);
				
				double x = randX+column*(0.05+random.nextDouble()*0.05);
				double y = randY+line*(0.1+random.nextDouble()*0.05);
				
				robot.teleportTo(new Vector2d(x,y));
				robot.setOrientation(random.nextDouble()*Math.PI*2);
				
				if(robot.isInvolvedInCollison()) {
					i--;
				}else
					column++;
			}
		} else {
			for(Robot r : simulator.getRobots()) {
				double randX = random.nextDouble()*(arenaWidth-0.2)+corridorWidth/2+0.1;
				double randY = random.nextDouble()*(arenaHeight-0.2) - arenaHeight/2 + 0.1;
				
				if(startingRoom < 0) {
					randX = -randX;
				}
				r.teleportTo(new Vector2d(randX,randY));
				r.setOrientation(random.nextDouble()*Math.PI*2);
			}
		}
	}
	
	@Override
	public void update(double time) {
		
		if(openDoorAuto)
			openDoor(1);
		
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
		
		if(doorsOpen) {
			doorTime++;
			if(doorTime > doorOpenTime)
				closeDoor();
			else if(autoCloseDoor && !firstDoorClosed) {
//				boolean insideDoor = true;
//				for(Robot r : robots) {
//					if(r.getPosition().getX() > corridorWidth/2-0.05 || r.getPosition().getX() < -(corridorWidth/2-0.05))
//						insideDoor = false;
//				}
				if(allRobotsInsideCorridor()) {
					if(currentDelayDoor >= delayDoor) {
						currentDelayDoor = 0;
						closeDoor();
					} else {
						currentDelayDoor++;
					}
				}
			}
		}
	}
	
	public boolean openDoor(double x) {
		
		if(specialDoor) {
			
			if(allowMultipleOpening && doorsOpen && firstDoorClosed) {
				for(Wall w : doors) {
					if(Math.abs(w.getPosition().getX()) > 10) {
						w.setPosition(w.getPosition().x+20, w.getPosition().y);
						w.moveWall();
					}
				}
				
				doorsOpen = false;
				firstDoorClosed = false;
			}
			
			if(!doorsOpen) {
				doorTime = 0;
				doorsOpen = true;
				firstDoorClosed = false;
				for(Wall w : doors) {
					if((x > 0 && w.getPosition().getX() > 0) || x < 0 && w.getPosition().getX() < 0) {
						w.setPosition(w.getPosition().x-20, w.getPosition().y);
						w.moveWall();
					}
				}
				return true;
			}else {
				if(holdDoor)
					return true;
				return false;
			}
		} else
			return super.openDoor(x);
	}
	
	@Override
	public void closeDoor() {
		if(specialDoor) {
			if(doorsOpen) {
				
				if(holdDoor && !firstDoorClosed && !allRobotsInsideCorridor())
					return;
				
				if(holdDoor && firstDoorClosed && !allRobotsOutsideCorridor())
					return;
				
				for(Wall w : doors) {
					if(Math.abs(w.getPosition().getX()) > 10) {
						w.setPosition(w.getPosition().x+20, w.getPosition().y);
						w.moveWall();
					}else if(!firstDoorClosed) {
						w.setPosition(w.getPosition().x-20, w.getPosition().y);
						w.moveWall();
					}
				}
				
				if(firstDoorClosed) {
					doorsOpen = false;
				}else {
					firstDoorClosed = true;
					doorTime = 0;
				}
			}
		}else
			super.closeDoor();
	}
	
	protected boolean allRobotsOutsideCorridor() {
		for(Robot r : robots) {
			double x = r.getPosition().getX();
			if(x < corridorWidth/2 && x > -corridorWidth/2)
				return false;
		}
		return true;
	}
	
	protected boolean allRobotsInsideCorridor() {
		for(Robot r : robots) {
			double x = r.getPosition().getX();
			if(!(x < corridorWidth/2-0.1 && x > -corridorWidth/2))
				return false;
		}
		return true;
	}
	
	protected void createRoom(Simulator simulator) {
		
		if(minMaxSize) {
			
			arenaWidth = random.nextDouble()*(maxSize-minSize) + minSize;
			arenaHeight = random.nextDouble()*(maxSize-minSize) + minSize;
			
		} else {
		
			if(arenaWidth == 0)
				arenaWidth = random.nextDouble()*maxSize + maxSize*2;
			if(arenaHeight == 0)
				arenaHeight = random.nextDouble()*maxSize + maxSize*2;
		
		}
		
		double centerX = -corridorWidth/2 - arenaWidth/2;
		
		//left
		createWall(simulator,centerX-arenaWidth/2,0,0.1,arenaHeight+0.1,PhysicalObjectType.WALL);
		createWall(simulator,centerX,arenaHeight/2,arenaWidth,0.1,PhysicalObjectType.WALL);
		createWall(simulator,centerX,-arenaHeight/2,arenaWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		//right
		createWall(simulator,-centerX+arenaWidth/2,0,0.1,arenaHeight+0.1,PhysicalObjectType.WALL);
		createWall(simulator,-centerX,arenaHeight/2,arenaWidth,0.1,PhysicalObjectType.WALL);
		createWall(simulator,-centerX,-arenaHeight/2,arenaWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		//center
		
		double x = -centerX-arenaWidth/2;
		double y = -arenaHeight/2/2-exitWidth/2;
		double height = (arenaHeight-exitWidth+0.05)/2;
		
		//center left
		createWall(simulator,-x,y,0.1,height,PhysicalObjectType.WALL);
		createWall(simulator,-x,-y,0.1,height,PhysicalObjectType.WALL);
		
		//center right
		createWall(simulator,x,y,0.1,height,PhysicalObjectType.WALL);
		createWall(simulator,x,-y,0.1,height,PhysicalObjectType.WALL);
		
		createWall(simulator,0,exitWidth,corridorWidth+0.1,0.1,PhysicalObjectType.WALL);
		createWall(simulator,0,-exitWidth,corridorWidth+0.1,0.1,PhysicalObjectType.WALL);
		
		//buttons
		if(useDoors) {
			Wall d1 = createWall(simulator,-corridorWidth/2-0.025,0,0.05,exitWidth+0.05,PhysicalObjectType.WALL);
			Wall d2 = createWall(simulator,corridorWidth/2+0.025,0,0.05,exitWidth+0.05,PhysicalObjectType.WALL);
			d1.color = Color.BLACK;
			d2.color = Color.BLACK;
			doors.add(d1);
			doors.add(d2);
			buttons.add(createWall(simulator,-corridorWidth/2,-0.25,0.1,0.1,PhysicalObjectType.WALLBUTTON));
			buttons.add(createWall(simulator,corridorWidth/2,0.25,0.1,0.1,PhysicalObjectType.WALLBUTTON));
		}
	}
	
	public boolean isFirstDoorClosed() {
		return firstDoorClosed;
	}
}