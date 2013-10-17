package environments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoRoomsMultiEnvironment extends TwoRoomsEnvironment {
	
	private double maxSize = 0.4;
	private double minSize = 0.75;
	private boolean aggregateRobots = true;
	private boolean specialDoor = false;
	private boolean firstDoorClosed = false;
	private boolean openDoorAuto = false;
	private boolean autoCloseDoor = false;
	private boolean minMaxSize = false;
	private boolean holdDoor = false;
	
	public TwoRoomsMultiEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator,arguments);
		maxSize = arguments.getArgumentAsDoubleOrSetDefault("maxsize",maxSize);
		aggregateRobots = arguments.getArgumentAsIntOrSetDefault("aggregaterobots", 1) == 1;
		specialDoor = arguments.getArgumentAsIntOrSetDefault("specialdoor", 0) == 1;
		autoCloseDoor = arguments.getArgumentAsIntOrSetDefault("autoclosedoor", 0) == 1;
		openDoorAuto = arguments.getArgumentAsIntOrSetDefault("opendoorauto", 0) == 1;
		minMaxSize = arguments.getArgumentAsIntOrSetDefault("minmaxsize", 0) == 1;
		holdDoor = arguments.getArgumentAsIntOrSetDefault("holddoor", 0) == 1;
		
		if(minMaxSize) {
			minSize = arguments.getArgumentAsDoubleOrSetDefault("minsize", minSize);
		}
	}
	
	@Override
	protected void placeRobots() {
		
		int numberOfRobots = simulator.getRobots().size();
		
		if(aggregateRobots) {
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
				if(allRobotsInsideCorridor())
					closeDoor();
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
			if(!(x < corridorWidth/2 && x > -corridorWidth/2))
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
		double height = (arenaHeight-exitWidth-0.2)/2;
		
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
			doors.add(createWall(simulator,-corridorWidth/2,0,0.1,exitWidth+0.05,PhysicalObjectType.WALL));
			doors.add(createWall(simulator,corridorWidth/2,0,0.1,exitWidth+0.05,PhysicalObjectType.WALL));
			buttons.add(createWall(simulator,-corridorWidth/2,-0.25,0.1,0.1,PhysicalObjectType.WALLBUTTON));
			buttons.add(createWall(simulator,corridorWidth/2,0.25,0.1,0.1,PhysicalObjectType.WALLBUTTON));
		}
	}
	
	public boolean isFirstDoorClosed() {
		return firstDoorClosed;
	}
}