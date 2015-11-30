package roommaze;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import comm.FileProvider;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class TMazeEnvironment extends Environment {

	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="7")
	private	double forbiddenArea;
	
	@ArgumentsAnnotation(name="randomize", values={"1","0"})
	private boolean randomize = false;
	
	@ArgumentsAnnotation(name="squaresize", defaultValue="0.3")
	protected double squareSize = 0.3;
	
	@ArgumentsAnnotation(name="mirror", values={"0","1"})
	private boolean mirror = false;
	
	@ArgumentsAnnotation(name="inverse", values={"0","1"})
	protected boolean inverse = false;
	
	@ArgumentsAnnotation(name="teleport", values={"0","1"})
	private boolean teleport = false;
	
	@ArgumentsAnnotation(name="widthchange", defaultValue="0")
	private double widthChange = 0;
	
	@ArgumentsAnnotation(name="randomizeorientation", values={"0","1"})
	private double randomizeOrientation = 0;
	
	@ArgumentsAnnotation(name="mazename", defaultValue="tmaze")
	private String mazeName;
	
	@ArgumentsAnnotation(name="numberofmazes", defaultValue="0")
	private int numberOfMazes;
	@ArgumentsAnnotation(name="numberofdifferentsamples", defaultValue="tmaze")
	private int numberOfDifferentSamples;
	
	@ArgumentsAnnotation(name="randomizex", defaultValue="0")
	protected double randomizeX = 0;
	
	@ArgumentsAnnotation(name="randomizey", defaultValue="0")
	protected double randomizeY = 0;
	
	private int    currentSample;
	private LinkedList<Square> squares = new LinkedList<Square>();
	private LinkedList<Square> allSquares = new LinkedList<Square>();
	private LinkedList<Square> forbiddenSquares = new LinkedList<Square>();
	
	private int imageSize = 400;
	private LinkedList<LightPole> lights = new LinkedList<LightPole>();
	private boolean killSample = false;
	
	private LinkedList<Boolean> lightPoleEnabled = new LinkedList<Boolean>();
	private boolean firstWall = true;
	
	protected Random random;
	protected int fitnesssample = 0;
	protected double positionX = 0;
	protected double positionY = 0;

	public TMazeEnvironment(Simulator simulator, Arguments arguments) {
		this(simulator,arguments,true);
	}
	
	public TMazeEnvironment(Simulator simulator, Arguments arguments, boolean firstWall) {
		super(simulator, arguments);
		this.random = simulator.getRandom();
		
		this.firstWall = firstWall;
		
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")	: 7;
		currentSample = arguments.getArgumentIsDefined("fitnesssample") ? arguments.getArgumentAsInt("fitnesssample")	: 0;
		randomize = arguments.getArgumentAsIntOrSetDefault("randomize",1) == 1;
		squareSize = arguments.getArgumentAsDoubleOrSetDefault("squaresize",squareSize);
		
		mazeName = arguments.getArgumentAsStringOrSetDefault("mazename", "tmaze");

		inverse = arguments.getArgumentAsInt("inverse") == 1;
		mirror = arguments.getArgumentAsInt("mirror") == 1;
		
		widthChange = arguments.getArgumentAsDoubleOrSetDefault("widthchange",0.0);
		randomizeY = arguments.getArgumentAsDoubleOrSetDefault("randomizey",0.0);
		randomizeOrientation = Math.toRadians(arguments.getArgumentAsDoubleOrSetDefault("randomizeorientation",0.0));
		
		randomizeX = arguments.getArgumentAsDoubleOrSetDefault("randomizex",0.0);
		randomizeY = arguments.getArgumentAsDoubleOrSetDefault("randomizey",0.0);
		
		positionX = arguments.getArgumentAsDoubleOrSetDefault("positionx",0.0);
		positionY = arguments.getArgumentAsDoubleOrSetDefault("positiony",0.0);
		
		teleport = arguments.getArgumentAsIntOrSetDefault("teleport",0) == 1;
		
		numberOfMazes = arguments.getArgumentAsIntOrSetDefault("numberofmazes", 0);
		numberOfDifferentSamples = arguments.getArgumentAsIntOrSetDefault("numberofdifferentsamples",1);
		
		double wC = random.nextDouble() * 3;
		
		if(wC > 1 && wC < 2)
			squareSize+=widthChange;
		else if(wC > 2)
			squareSize-=widthChange;
	}
	
	public void setup(Simulator simulator) {
		super.setup(simulator);
		if(numberOfMazes > 0)
		{
			int currentMaze = currentSample % numberOfMazes;
			
			try {
				FileProvider fp = simulator.getFileProvider();
				Scanner s = new Scanner(fp.getFile("mazes/"+mazeName+currentMaze+".txt"));
				s.useDelimiter("\n");
				if(!randomize || currentSample < numberOfDifferentSamples) {
					fitnesssample = currentSample % numberOfDifferentSamples;
					createMaze(simulator, s, fitnesssample);
				} else {
					fitnesssample = random.nextInt(numberOfDifferentSamples);
					createMaze(simulator, s, fitnesssample);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(teleport) {
			
			double orientation = robots.get(0).getOrientation()+(random.nextDouble()*2-1)*this.randomizeOrientation;
			double randomizeX = this.randomizeX*random.nextDouble()*2-this.randomizeX;
			double randomizeY = this.randomizeY*random.nextDouble()*2-this.randomizeY;
			double x = getSquares().peek().getX();
			double y = getSquares().peek().getY();
			
			x+=positionX;
			y+=positionY;
			
			robots.get(0).moveTo(new Vector2d(x+randomizeX,y+randomizeY));
			robots.get(0).setOrientation(orientation);
		}
		
		if(inverse) {
			double orientation = Math.PI/2;
			
			if(randomizeOrientation > 0)
				orientation+=random.nextDouble()*randomizeOrientation;
			
			double randomY = 0;
			
			if(randomizeY > 0) {
				randomY = random.nextDouble()*randomizeY*-1;
			}
			
			if(fitnesssample == 1 || fitnesssample == 3) {
				randomY*=-1;
				orientation+=Math.PI;
			}
			
			robots.get(0).teleportTo(new Vector2d(getSquares().peek().getX(),getSquares().peek().getY()+randomY));
			robots.get(0).setOrientation(orientation);
		}
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
	
	private void createMaze(Simulator simulator, Scanner s, int currentSample) {
		
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
				createWall(simulator, sq.x,sq.y+squareSize/2,squareSize+0.1,wallSize);
			if(sq.walls[1]==1)
				createWall(simulator, sq.x+squareSize/2,sq.y,wallSize,squareSize+0.1);
			if(sq.walls[2]==1)
				createWall(simulator, sq.x,sq.y-squareSize/2,squareSize+0.1,wallSize);
			if(sq.walls[3]==1)
				createWall(simulator, sq.x-squareSize/2,sq.y,wallSize,squareSize+0.1);
		}
	}
	
	protected void createWall(Simulator simulator, double x, double y, double width, double height) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,PhysicalObjectType.WALL);
		this.addObject(w);
	}
	
	public int getSample() {
		return currentSample;
	}
	
	@Override
	public void update(double time) {
		
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
		double robotDiameter = r.getDiameter();
		Rectangle2D.Double robotRect = new Rectangle2D.Double(r.getPosition().getX()-robotDiameter/2,r.getPosition().getY()-robotDiameter/2,robotDiameter,robotDiameter);
		
		return squareRect.intersects(robotRect);
	}

	public double getForbiddenArea() {
		return forbiddenArea;
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