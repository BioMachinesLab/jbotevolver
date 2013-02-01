package simulation.environment;

import gui.renderer.Renderer;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TMazeEnvironment extends Environment {

	private	double forbiddenArea;
	private int    currentSample;
	private LinkedList<Square> squares = new LinkedList<Square>();
	private LinkedList<Square> allSquares = new LinkedList<Square>();
	private LinkedList<Square> forbiddenSquares = new LinkedList<Square>();
	private double squareSize = 0.3;
	private int imageSize = 400;
	private LinkedList<LightPole> lights = new LinkedList<LightPole>();
	private boolean killSample = false;
	private boolean randomize = false;
	private LinkedList<Boolean> lightPoleEnabled = new LinkedList<Boolean>();
	private boolean firstWall = true;
	protected boolean inverse = false;
	private boolean teleported = false;
	private int fitnesssample = 0;
	private boolean mirror = false;
	private boolean mixed = false;
	private boolean onlyMaze = false;
	private int mixedSamples = 0;
	private boolean teleport = false;
	private double widthChange = 0;
	private double randomizeOrientation = 0;
	private double randomizeY = 0;

	public TMazeEnvironment(Simulator simulator, Arguments arguments) {
		this(simulator,arguments,true);
	}
	
	public TMazeEnvironment(Simulator simulator, Arguments arguments, boolean firstWall) {
		
		super(simulator,arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 7, 
				  arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 7);
		
		this.firstWall = firstWall;
		
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")	: 7;
		currentSample = arguments.getArgumentIsDefined("fitnesssample") ? arguments.getArgumentAsInt("fitnesssample")	: 0;
		
		randomize = arguments.getArgumentAsIntOrSetDefault("randomize",1) == 1;
		squareSize = arguments.getArgumentAsDoubleOrSetDefault("squaresize",squareSize);
		
		String mazeName = arguments.getArgumentAsStringOrSetDefault("mazename", "tmaze");

		inverse = arguments.getArgumentAsInt("inverse") == 1;
		mirror = arguments.getArgumentAsInt("mirror") == 1;
		
		mixed = arguments.getArgumentAsIntOrSetDefault("mixed",0) == 1;
		
		mixedSamples = arguments.getArgumentAsIntOrSetDefault("mixedsamples",12);
		
		widthChange = arguments.getArgumentAsDoubleOrSetDefault("widthchange",0.0);
		
		randomizeY = arguments.getArgumentAsDoubleOrSetDefault("randomizey",0.0);
		randomizeOrientation = arguments.getArgumentAsDoubleOrSetDefault("randomizeorientation",0.0);
		
		double wC = simulator.getRandom().nextDouble() * 3;
		
		if(wC > 1 && wC < 2)
			squareSize+=widthChange;
		else if(wC > 2)
			squareSize-=widthChange;
		
		teleport = arguments.getArgumentAsIntOrSetDefault("teleport",0) == 1;
		
		if(mixed) {
			onlyMaze = currentSample % mixedSamples < 4*2; //0-7
			inverse = currentSample % mixedSamples < 4;//0-3
			if(inverse && !mazeName.equals("inversetmaze"))
				mazeName = "inversetmaze";
		}
		
		if(arguments.getArgumentIsDefined("numberofmazes"))
		{
			int numberOfMazes = arguments.getArgumentAsInt("numberofmazes");
			int numberOfDifferentSamples = arguments.getArgumentAsInt("numberofdifferentsamples");

			int currentMaze = currentSample % numberOfMazes;
			
			try {
				Scanner s = new Scanner(simulator.getFileProvider().getFile("mazes/"+mazeName+currentMaze+".txt"));
				s.useDelimiter("\n");
				if(!randomize || currentSample < numberOfDifferentSamples) {
					fitnesssample = currentSample % numberOfDifferentSamples;
					createMaze(s, fitnesssample);
				} else {
					fitnesssample = simulator.getRandom().nextInt(numberOfDifferentSamples);
					createMaze(s, fitnesssample);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isMixed() {
		return mixed;
	}
	
	public int getMixedSample() {
		return currentSample % mixedSamples;
	}
	
	protected void applyOffset(double x, double y) {
		
		for(Square sq : allSquares) {
			sq.x = x+sq.x;
			sq.y = y+sq.y;
		}
		
		for(PhysicalObject obj : getAllObjects()) {
			obj.getPosition().setX(x+obj.getPosition().getX());
			obj.getPosition().setY(y+obj.getPosition().getY());
			
			if(obj instanceof Wall)
				((Wall)obj).moveWall();
		}
	}
	
	private void createMaze(Scanner s, int currentSample) {
		
		squares = new LinkedList<Square>();
		allSquares = new LinkedList<Square>();
		lights = new LinkedList<LightPole>();
		forbiddenSquares = new LinkedList<Square>();
		
		int sampleOrder[] = new int[1];//initialization because of compiler errors
		int sampleNumber = 0;
		LinkedList<Integer> forbidden = new LinkedList<Integer>();
		
		while(s.hasNext()) {
			
			String[] input = s.next().trim().split(";");
			
			if(input[0].equals("L")) {
				
				boolean insertLight = false;
				
				for(int i = 3 ; i < input.length ; i++)
					if(Integer.parseInt(input[i]) == currentSample) {
						insertLight = true;
						break;
					}
				if(insertLight) {
					double coords[] = {Double.parseDouble(input[1])*squareSize, Double.parseDouble(input[2])*squareSize};
					LightPole l = new LightPole(simulator, "", coords[0], coords[1], 0.25);
					l.setTurnedOn(false);
					lightPoleEnabled.add(true);
					lights.add(l);
					addObject(l);
				}
			} else if(input[0].equals("S")) {
				//Sample Order
				if(currentSample == sampleNumber++) {
					sampleOrder = new int[input.length-1];
					for(int i = 1, j = 0 ; i < input.length ; i++, j++) {
						sampleOrder[j] = Integer.parseInt(input[i]);
					}
				}else {
					if(inverse)
						forbidden.add(Integer.parseInt(input[1]));
					else
						forbidden.add(Integer.parseInt(input[input.length-1]));
				}
				
			} else if(input[0].equals("W")) {
				Square sq = new Square();
				sq.x = Double.parseDouble(input[1])*squareSize;
				sq.y = Double.parseDouble(input[2])*squareSize;
				
				for(int i = 0 ; i < 4 ; i++)
					sq.walls[i] = Integer.parseInt(input[3+i]);
				
				if(mirror) {
					sq.x = Double.parseDouble(input[1])*-squareSize;
					int tempWall = sq.walls[1];
					sq.walls[1] = sq.walls[3];
					sq.walls[3] = tempWall;
				}
				
				if(allSquares.isEmpty() && !firstWall)
					sq.walls[2] = 0;
				
				allSquares.add(sq);
			}
		}
		
		//preparesample
		for(int i = 0 ; i < sampleOrder.length ; i++)
			squares.add(allSquares.get(sampleOrder[i]));
		
		for(int i = 0 ; i < forbidden.size() ; i++)
			forbiddenSquares.add(allSquares.get(forbidden.get(i)));
		
		for(int i = squares.size()-2 ; i >= 0 ; i--) {
			Square current = squares.get(i);
			Square next = squares.get(i+1);
			
			double currentX = current.x;
			double currentY = current.y;
			
			double nextX = next.x;
			double nextY = next.y;
			
			double distanceX = Math.abs(currentX-nextX);
			double distanceY = Math.abs(currentY-nextY);
			
			current.distanceToFinish = next.distanceToFinish + distanceX + distanceY;
		}
		
		for(int i = 0 ; i < allSquares.size() ; i++) {
			Square sq = allSquares.get(i);
			
			double wallSize = 0.1;
			
			if(sq.walls[0]==1)
				createWall(sq.x,sq.y+squareSize/2,squareSize+0.1,wallSize);
			if(sq.walls[1]==1)
				createWall(sq.x+squareSize/2,sq.y,wallSize,squareSize+0.1);
			if(sq.walls[2]==1)
				createWall(sq.x,sq.y-squareSize/2,squareSize+0.1,wallSize);
			if(sq.walls[3]==1)
				createWall(sq.x-squareSize/2,sq.y,wallSize,squareSize+0.1);
		}
	}
	
	protected void createWall(double x, double y, double width, double height) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,1,0,width,height,PhysicalObjectType.WALL);
		this.addObject(w);
	}
	
	public int getSample() {
		return currentSample;
	}
	
	@Override
	public void update(int time) {
		super.update(time);
		
		if(!teleported && robots.size() > 1) {
			double orientation = -Math.PI/2;
			double deltaY = -0.05;
			
			if(fitnesssample == 0 || fitnesssample == 2) {
				orientation+=Math.PI;
				deltaY*=-1;
			}
			
			double deltaX = 0.2;
			
			for(int i = 1 ; i < robots.size() ; i++) {
				robots.get(i).teleportTo(new Vector2d(getSquares().peekLast().getX()+deltaX,getSquares().peekLast().getY()+deltaY));
				robots.get(i).setOrientation(orientation);
			}
			teleported = true;
		}
		
		if(teleport && ! teleported) {
//			double randomY = simulator.getRandom().nextDouble()*0.2-0.5;
			robots.get(0).teleportTo(new Vector2d(getSquares().peek().getX(),getSquares().peek().getY()));
			teleported = true;
		}
		
		//jesus christ... crappy code ahoy!
		if(onlyMaze && !inverse && mixed && !teleported) {
			double randomY = simulator.getRandom().nextDouble()*0.2-0.5;
			robots.get(0).teleportTo(new Vector2d(getSquares().peek().getX(),getSquares().peek().getY()+randomY));
			robots.get(0).setOrientation(Math.PI/2);
			teleported = true;
		}
		
		if(inverse && !teleported) {
//			double randomY = simulator.getRandom().nextDouble()*0.2-0.2;
			double orientation = Math.PI/2;
			
			if(randomizeOrientation > 0)
				orientation+=simulator.getRandom().nextDouble()*randomizeOrientation;
			
			double randomY = 0;
			
			if(randomizeY > 0) {
				randomY = simulator.getRandom().nextDouble()*randomizeY*-1;
			}
			
			if(fitnesssample == 1 || fitnesssample == 3) {
				randomY*=-1;
				orientation+=Math.PI;
			}
			
			robots.get(0).teleportTo(new Vector2d(getSquares().peek().getX(),getSquares().peek().getY()+randomY));
			robots.get(0).setOrientation(orientation);
			teleported = true;
		}
		
		for(int i = 0 ; i < lights.size() ; i++) {
			if(lightPoleEnabled.get(i)) {
				LightPole l = lights.get(i);
				double robotY = robots.get(0).getPosition().getY();
				if(Math.abs((l.getPosition().getY()-robotY)) < 0.1)
					l.setTurnedOn(true);
				else {
					if(l.isTurnedOn()) {
						l.setTurnedOn(false);
						lightPoleEnabled.set(i, false);
					}
				}
			}
		}
		if(checkIntersection(squares.peek(), robots.get(0)))
			resetLights();
	}
	
	private void resetLights() {
		for(int i = 0 ; i < lightPoleEnabled.size() ; i++)
			lightPoleEnabled.set(i, true);
	}
	
	public boolean checkIntersection(Square sq, Robot r) {
		double squareSize = getSquareSize()/2;
		
		Rectangle2D.Double squareRect = new Rectangle2D.Double(sq.getX()-squareSize/2,sq.getY()-squareSize/2,squareSize,squareSize);
		double robotDiameter = r.getDiameter()+r.getExtraRadius()*2;
		Rectangle2D.Double robotRect = new Rectangle2D.Double(r.getPosition().getX()-robotDiameter/2,r.getPosition().getY()-robotDiameter/2,robotDiameter,robotDiameter);
		
		return squareRect.intersects(robotRect);
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	@Override
	public void draw(Renderer renderer){
		robots.get(0).setBodyColor(Color.GRAY);
		renderer.drawCircle(new Point2d(0, 0), forbiddenArea);
	}
	
	public LinkedList<Square> getSquares() {
		return squares;
	}
	
	public LinkedList<Square> getForbiddenSquares() {
		return forbiddenSquares;
	}
	
	public double getSquareSize() {
		return squareSize;
	}
	
	public double getImageSize() {
		return imageSize;
	}
	
	public static class Square {
		double x;
		double y;
		int walls[] = {0,0,0,0};
		double distanceToFinish = 0;
		
		public double getDistance() {
			return distanceToFinish;
		}
		
		public int[] getWalls() {
			return walls;
		}
		
		public double getX() {
			return x;
		}
		
		public double getY() {
			return y;
		}
		
		@Override
		public String toString() {
			return x+" "+y+" "+distanceToFinish;
		}
	}

	public boolean killSample() {
		return killSample;
	}
	
	public void killSample(boolean b) {
		killSample = b;
	}
	
	public LinkedList<LightPole> getLights() {
		return lights;
	}
}