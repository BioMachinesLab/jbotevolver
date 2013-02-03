package simulation.environment;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.util.Arguments;

public class ClutteredMazeEnvironment extends TMazeEnvironment {
	
	private double exitWidth = 0.15;
	private double arenaWidth = 0;
	private double arenaHeight = 0;
	private int objects = 3;
	private double objectWidth = 0.15+0.1;
	private Vector2d exitPosition;
	double maxWidth,maxHeight,minWidth,minHeight;

	public ClutteredMazeEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments, false);
		maxWidth = arguments.getArgumentAsDoubleOrSetDefault("maxwidth",this.width-1);
		maxHeight = arguments.getArgumentAsDoubleOrSetDefault("maxheight",this.height-1);
		
		minWidth = arguments.getArgumentAsDoubleOrSetDefault("minwidth",maxWidth);
		minHeight = arguments.getArgumentAsDoubleOrSetDefault("minheight",maxHeight);
		exitWidth = arguments.getArgumentAsDoubleOrSetDefault("exitwidth",exitWidth);
		objects = arguments.getArgumentAsIntOrSetDefault("objects",objects);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		createRoom(simulator, minWidth,maxWidth,minHeight,maxHeight);
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
			offsetY = sq.getY()-arenaHeight/2-exitWidth*4;
		}
		
		double exitX = -arenaWidth/2;
		
		while(exitX < -arenaWidth/2+exitWidth+0.2 || exitX > arenaWidth/2-exitWidth-0.2) {
			double exitPercentage = random.nextDouble();
			exitX = -(arenaWidth/2)+arenaWidth*exitPercentage;
		}
		
		applyOffset(exitX,-offsetY);
		
		exitPosition = new Vector2d();
		exitPosition.x = exitX;
		exitPosition.y = arenaHeight/2+exitWidth;
		
		//Exit
		createWall(simulator,exitX-exitWidth,arenaHeight/2+0.2,0.1,0.4);
		createWall(simulator,exitX+exitWidth,arenaHeight/2+0.2,0.1,0.4);
		
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