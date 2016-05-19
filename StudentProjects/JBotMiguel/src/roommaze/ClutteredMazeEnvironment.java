package roommaze;

import controllers.DummyController;
import epuck.Epuck;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class ClutteredMazeEnvironment extends TMazeEnvironment {
	
	@ArgumentsAnnotation(name="exitwidth", defaultValue="0.15")
	private double exitWidth = 0.15;
	private double arenaWidth = 0;
	private double arenaHeight = 0;
	@ArgumentsAnnotation(name="objects", defaultValue="3")
	private int objects = 3;
	@ArgumentsAnnotation(name="objectwidth", defaultValue="0.25")
	private double objectWidth = 0.15+0.1;
	private Vector2d exitPosition;
	
	@ArgumentsAnnotation(name="maxwidth", defaultValue="1")
	private double maxWidth = 1;
	@ArgumentsAnnotation(name="maxheight", defaultValue="1")
	private double maxHeight = 1;
	@ArgumentsAnnotation(name="minwidth", defaultValue="1")
	private double minWidth = 1;
	@ArgumentsAnnotation(name="minheight", defaultValue="1")
	private double minHeight = 1;
	
	@ArgumentsAnnotation(name="randomizemazey", values={"0","1"}, defaultValue="")
	private boolean randomizeMazeY = false;
	@ArgumentsAnnotation(name="extrarobot", values={"0","1"}, defaultValue="")
	private boolean extraRobot = false;

	public ClutteredMazeEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments, false);
		maxWidth = arguments.getArgumentAsDoubleOrSetDefault("maxwidth",maxWidth);
		maxHeight = arguments.getArgumentAsDoubleOrSetDefault("maxheight",maxWidth);
		
		minWidth = arguments.getArgumentAsDoubleOrSetDefault("minwidth",minWidth);
		minHeight = arguments.getArgumentAsDoubleOrSetDefault("minheight",minWidth);
		
		exitWidth = arguments.getArgumentAsDoubleOrSetDefault("exitwidth",exitWidth);
		objects = arguments.getArgumentAsIntOrSetDefault("objects",objects);
		
		extraRobot = arguments.getArgumentAsIntOrSetDefault("extrarobot",0) == 1;
		randomizeMazeY = arguments.getArgumentAsIntOrSetDefault("randomizemazey",0) == 1;
		
		objectWidth = arguments.getArgumentAsDoubleOrSetDefault("objectwidth",objectWidth);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		exitWidth = squareSize/2;
		createRoom(simulator, minWidth,maxWidth,minHeight,maxHeight);
		
		if(!robots.isEmpty()) {
			Robot robot = robots.get(0);
			double originalX = 0;
			double originalY = 0;
			int tries = 0;
			do{
				if(++tries % 10 == 0) {
					randomizeX+=0.1;
					randomizeY+=0.1;
				}
				
				double x = originalX;
				double y = originalY;
			
				if(randomizeX > 0) {
					double distance = (simulator.getRandom().nextDouble()*2-1)*randomizeX;
					x-=distance;
				}
				if(randomizeY > 0) {
					double distance = (simulator.getRandom().nextDouble()*2-1)*randomizeY;
					y+=distance;
				}
				
				robot.moveTo(new Vector2d(x, y));
				
				updateRobotCloseObjects(0);
				updateCollisions(0);
			} while(robot.isInvolvedInCollison());
	
			if(extraRobot) {
				
				Robot r = new Epuck(simulator, new Arguments(""));
				r.setController(new DummyController(simulator,r,new Arguments("")));
				
				double deltaY = -0.05;
				
				if(fitnesssample == 0 || fitnesssample == 2) {
					deltaY*=-1;
				}
				
				double deltaX = 0.2;
				
				r.moveTo(new Vector2d(getSquares().peekLast().getX()+deltaX,getSquares().peekLast().getY()+deltaY));
				addRobot(r);
			} 
		}
	}
	
	protected void createRoom(Simulator simulator, double minWidth, double maxWidth, double minHeight, double maxHeight) {
		
		arenaWidth = minWidth;
		arenaHeight = minHeight;
		
		double widthDiff = Math.abs(maxWidth-minWidth);
		double heightDiff = Math.abs(maxHeight-minHeight);
		
		arenaWidth+= random.nextDouble()*widthDiff;
		arenaHeight+= random.nextDouble()*heightDiff;
		
		double offsetY = arenaHeight/2-exitWidth*4;
		
		if(this.getSquares() != null && !this.getSquares().isEmpty()) {
			Square sq = getSquares().peek();
			if(inverse)
				sq = getSquares().peekLast();
			double rand = 0;
			if(randomizeMazeY)
				rand = simulator.getRandom().nextDouble()*0.25;
			
			offsetY = sq.getY()-arenaHeight/2-exitWidth*4+rand;
		}
		
		double exitX = -arenaWidth/2;
		int count = 0;
		while(exitX < -arenaWidth/2+exitWidth+0.1 || exitX > arenaWidth/2-exitWidth-0.1) {
			double exitPercentage = random.nextDouble();
			exitX = -(arenaWidth/2)+arenaWidth*exitPercentage;
			count++;
			if(count > 100)
				break;
		}
		if(count >= 100)
			exitX = 0;
		
		applyOffset(exitX,-offsetY);
		
		exitPosition = new Vector2d();
		exitPosition.x = exitX;
		exitPosition.y = arenaHeight/2+exitWidth;
		
		//Exit
		createWall(simulator,exitX-exitWidth,arenaHeight/2+0.2,0.1,0.45);
		createWall(simulator,exitX+exitWidth,arenaHeight/2+0.2,0.1,0.45);
		
		double firstDistanceEdge = exitX-exitWidth-(-arenaWidth/2);
		double firstPartX = -arenaWidth/2+firstDistanceEdge/2;
		
		double secondDistanceEdge = arenaWidth/2-exitX-exitWidth;
		double secondPartX = arenaWidth/2-secondDistanceEdge/2;
				
		//Horizontal - Top
		createWall(simulator,firstPartX,arenaHeight/2,firstDistanceEdge+0.1,0.1);
		createWall(simulator,secondPartX,arenaHeight/2,secondDistanceEdge+0.1,0.1);
		
		//Horizontal - Bottom
		createWall(simulator,0,-arenaHeight/2,arenaWidth+0.1,0.1);
		
		//Vertical
		createWall(simulator,arenaWidth/2,0,0.1,arenaHeight);
		createWall(simulator,-arenaWidth/2,0,0.1,arenaHeight);
		
		objects = (int)(objects*(arenaWidth*arenaHeight));
		
		for(int i = 0 ; i < objects ; i++) {
			
			double objectW = objectWidth+objectWidth*(random.nextDouble()-0.5);
				
			double objectX = -arenaWidth/2 + 0.1 + random.nextDouble()*(arenaWidth-0.1*2);
			double objectY = (arenaHeight/2 - objectW - random.nextDouble()*(arenaHeight/2))*0.6;
				
			createWall(simulator,objectX,objectY,objectW,objectW);
		}
	}
	
	public Vector2d getRoomExitPosition() {
		return exitPosition;
	}
	
	public double getExitWidth() {
		return exitWidth;
	}
}