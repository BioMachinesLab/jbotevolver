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

	public ClutteredMazeEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments, false);
		double maxWidth = arguments.getArgumentAsDoubleOrSetDefault("maxwidth",this.width-1);
		double maxHeight = arguments.getArgumentAsDoubleOrSetDefault("maxheight",this.height-1);
		
		double minWidth = arguments.getArgumentAsDoubleOrSetDefault("minwidth",maxWidth);
		double minHeight = arguments.getArgumentAsDoubleOrSetDefault("minheight",maxHeight);
		exitWidth = arguments.getArgumentAsDoubleOrSetDefault("exitwidth",exitWidth);
		objects = arguments.getArgumentAsIntOrSetDefault("objects",objects);

		createRoom(minWidth,maxWidth,minHeight,maxHeight);
	}
	
	protected void createRoom(double minWidth, double maxWidth, double minHeight, double maxHeight) {
		
		arenaWidth = minWidth;
		arenaHeight = minHeight;
		
		double widthDiff = Math.abs(maxWidth-minWidth);
		double heightDiff = Math.abs(maxHeight-minHeight);
		
		arenaWidth+= simulator.getRandom().nextDouble()*widthDiff;
		arenaHeight+= simulator.getRandom().nextDouble()*heightDiff;
		
		double offsetY = arenaHeight/2-exitWidth*4;
		
		if(this.getSquares() != null && !this.getSquares().isEmpty()) {
			Square sq = getSquares().peek();
			if(inverse)
				sq = getSquares().peekLast();
			offsetY = sq.getY()-arenaHeight/2-exitWidth*4;
		}
		
		double exitX = -arenaWidth/2;
		
		while(exitX < -arenaWidth/2+exitWidth+0.2 || exitX > arenaWidth/2-exitWidth-0.2) {
			double exitPercentage = simulator.getRandom().nextDouble();
			exitX = -(arenaWidth/2)+arenaWidth*exitPercentage;
		}
		
		applyOffset(exitX,-offsetY);
		
		exitPosition = new Vector2d();
		exitPosition.x = exitX;
		exitPosition.y = arenaHeight/2+exitWidth;
		
		//Exit
		createWall(exitX-exitWidth,arenaHeight/2+0.2,0.1,0.4);
		createWall(exitX+exitWidth,arenaHeight/2+0.2,0.1,0.4);
		
		double firstDistanceEdge = exitX-exitWidth-(-arenaWidth/2);
		double firstPartX = -arenaWidth/2+firstDistanceEdge/2;
		
		double secondDistanceEdge = arenaWidth/2-exitX-exitWidth;
		double secondPartX = arenaWidth/2-secondDistanceEdge/2;
				
		//Horizontal - Top
		createWall(firstPartX,arenaHeight/2,firstDistanceEdge+0.1,0.1);
		createWall(secondPartX,arenaHeight/2,secondDistanceEdge+0.1,0.1);
		
		//Horizontal - Bottom
		createWall(0,-arenaHeight/2,arenaWidth+0.1,0.1);
		
		//Vertical
		createWall(arenaWidth/2,0,0.1,arenaHeight);
		createWall(-arenaWidth/2,0,0.1,arenaHeight);
		
		objects = (int)(objects*(arenaWidth*arenaHeight));
		
//		double allotedWidths = Math.max(arenaWidth,arenaHeight)/3;
		
		for(int i = 0 ; i < objects ; i++) {
			
//			if(allotedWidths > 0) {
				double objectW = objectWidth+objectWidth*(simulator.getRandom().nextDouble()-0.5);
				
//				allotedWidths-=objectW;
				
//				if(allotedWidths < 0)
//					objectW+=allotedWidths;
				
				double objectX = -arenaWidth/2 + 0.1 + simulator.getRandom().nextDouble()*(arenaWidth-0.1*2);
				double objectY = (arenaHeight/2 - objectW - simulator.getRandom().nextDouble()*(arenaHeight/2))*0.6;
				
				createWall(objectX,objectY,objectW,objectW);
//			}
		}
	}
	
	public Vector2d getRoomExitPosition() {
		return exitPosition;
	}
	
	public double getExitWidth() {
		return exitWidth;
	}

}
