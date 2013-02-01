package simulation.environment;


import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;
import gui.renderer.TwoDRendererDebug;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 * Allows the use of a background image and reads RGB of the image values from the robot's current position.
 * 
 * @author miguelduarte
 */
public class MazeEnvironment extends Environment {

	private	double forbiddenArea;
	private int    totalSamples;
	private int    currentSample;
	private Image image;
	private BufferedImage bufferedImage;
	private int[] rgb = {255,255,255};
	private LinkedList<Square> squares;
	private int squareSize = 10;
	private int imageSize = 0;

	public MazeEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")	: 7;
		totalSamples = arguments.getArgumentIsDefined("totalsamples") ? arguments.getArgumentAsInt("totalsamples")	: 5;
		currentSample = arguments.getArgumentIsDefined("fitnesssample") ? arguments.getArgumentAsInt("fitnesssample")	: 0;
		
		if(arguments.getArgumentIsDefined("numberofmazes"))
		{
			int numberOfMazes = arguments.getArgumentAsInt("numberofmazes");

			int currentMaze = currentSample % numberOfMazes;
			String mazeName = arguments.getArgumentAsString("maze"+currentMaze);
			try {
				Scanner s = new Scanner(new File("mazes/maze"+currentMaze+".txt"));
				createMaze(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Wall w = new Wall(simulator,"wall",0,-0.5,Math.PI,1,1,1,0,0.02,1,PhysicalObjectType.WALL);
		//this.addObject(w);
		
		findColoredSquares();
	}
	
	private void createMaze(Scanner s) {
		
		squares = new LinkedList<Square>();
		
		imageSize = 400;
		
		if(s.nextLine().trim().charAt(0) == 'W')
			squareSize = Integer.parseInt(s.nextLine().trim());
	
		squareSize = 30;
		
		while(s.hasNextLine()) {
			char type = s.nextLine().trim().charAt(0);
			String[] location = s.nextLine().trim().split(";");
			Square sq = new Square();
			sq.x = Integer.parseInt(location[0]);
			sq.y = imageSize-Integer.parseInt(location[1]);
			switch(type) {
				case 'S':
				case 'F':	
					sq.color = Color.BLACK;
				break;
				case 'R':
					sq.color = Color.RED;
				break;
				case 'B':
					sq.color = Color.BLUE;
				break;
			}
			squares.add(sq);
		}
		
		
		for(int i = squares.size()-2 ; i >= 0 ; i--) {
			Square current = squares.get(i);
			Square next = squares.get(i+1);
			
			int currentX = current.x;
			int currentY = current.y;
			
			int nextX = next.x;
			int nextY = next.y;
			
			int distanceX = Math.abs(currentX-nextX);
			int distanceY = Math.abs(currentY-nextY);
			
			current.distanceToFinish = next.distanceToFinish + distanceX + distanceY;
		}
		
		bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.createGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		
		g.setColor(Color.BLACK);
		for(int i = 0 ; i < squares.size() ; i++) {
			if(i+1 < squares.size()) {
				
				int sx = Math.min(squares.get(i).x,squares.get(i+1).x);
				int ex = Math.max(squares.get(i).x,squares.get(i+1).x);
				
				int sy = Math.min(squares.get(i).y,squares.get(i+1).y);
				int ey = Math.max(squares.get(i).y,squares.get(i+1).y);
				
				int sqWidth = Math.abs(ex-sx);
				int sqHeight = Math.abs(ey-sy);
				
				g.fillRect(sx-squareSize/2,sy-squareSize/2, Math.max(squareSize,sqWidth), Math.max(squareSize,sqHeight));
				
			}
		}
		
		for(Square sq : squares) {
			g.setColor(sq.color);
			g.fillRect(sq.x-squareSize/2,sq.y-squareSize/2, squareSize, squareSize);
		}
		
		image = Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
		
		for(int i = 0 ; i < squares.size() ; i++) {
			
			Square sq = squares.get(i);
			
			double[] coords = imageCoordToEnvCoord(sq.x,sq.y);
			double scale = bufferedImage.getWidth()/this.width;
			double width = squareSize/scale;
			
			double wallSize = 0.1;
			
			int[] directions = findDirectionSquare(i);
			//System.out.println("Sq:"+i+" "+directions[0]+" "+directions[1]+" "+directions[2]+" "+directions[3]);
			
			if(directions[0] == 0)
				createWall(coords[0],coords[1]+width/2,width+0.1,wallSize);
			if(directions[1] == 0)
				createWall(coords[0]+width/2,coords[1],wallSize,width+0.1);
			if(directions[2] == 0)
				createWall(coords[0],coords[1]-width/2,width+0.1,wallSize);
			if(directions[3] == 0)
				createWall(coords[0]-width/2,coords[1],wallSize,width+0.1);
			
			//not the last
			if(i < squares.size()-1) {
				Square sq2 = squares.get(i+1);
				double[] coords2 = imageCoordToEnvCoord(sq2.x,sq2.y);
				
				double distX = Math.abs(coords2[0]-coords[0]);
				double distY = Math.abs(coords2[1]-coords[1]);
				
				double middleX = (coords[0]+coords2[0])/2;
				double middleY = (coords[1]+coords2[1])/2;
				
				if(distX == 0) {
					createWall(middleX-width/2,middleY,wallSize,distY-width+0.1);
					createWall(middleX+width/2,middleY,wallSize,distY-width+0.1);
				}else if(distY == 0) {
					createWall(middleX,middleY+width/2,distX-width+0.1,wallSize);
					createWall(middleX,middleY-width/2,distX-width+0.1,wallSize);
				}
				
			}
		}
	}
	
	private int[] findDirectionSquare(int index) {
		
		int[] directions = new int[4];
		
		double current[] = imageCoordToEnvCoord(squares.get(index).x,squares.get(index).y);
		
		if(index > 0) {
			double previous[] = imageCoordToEnvCoord(squares.get(index-1).x,squares.get(index-1).y);
			directions[findDirection(previous,current)] = 1;
		}
		
		if(index < squares.size()-1) {
			double next[] = imageCoordToEnvCoord(squares.get(index+1).x,squares.get(index+1).y);
			directions[findDirection(next,current)] = 1;
		}
		
		return directions;
	}
	
	private int findDirection(double[] coord1, double[] coord2) {
		
		//0=up, 1=right, 2=down, 3=left
		
		if(coord1[0] == coord2[0]) //vertical difference
			return coord1[1] > coord2[1] ? 0 : 2;
		
		if(coord1[1] == coord2[1]) //horizontal difference
			return coord1[0] > coord2[0] ? 1 : 3;
			
		
		return 0;		
	}
	
	private void createWall(double x, double y, double width, double height) {
		
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,PhysicalObjectType.WALL);
		this.addObject(w);
	}
	
	/**
	 * Finds all colored squares and puts a robot with that color in the center
	 */
	private void findColoredSquares() {
		
		int[] previousColor = new int[]{255,255,255};
		
		for(int y = 0 ; y < bufferedImage.getHeight(); y++) {
			for(int x = 0 ; x < bufferedImage.getWidth(); x++) {
				
				int[] currentColor = findColor(x,y);
				
				if(!Arrays.equals(currentColor, previousColor)) {
					if(Arrays.equals(currentColor, new int[]{255,0,0}) ||
							Arrays.equals(currentColor, new int[]{0,255,0}) ||
							Arrays.equals(currentColor, new int[]{0,0,255})) {
						
						if(!Arrays.equals(currentColor, findColor(x,y+1)))
						{
							double[] coords = imageCoordToEnvCoord(x,y);
							
							String color = "red";
							if(currentColor[1]==255) color = "green";
							if(currentColor[2]==255) color = "blue";
							
							double increment = 0.25;//size of the square... hardcoded! =/
							
							//Robot r = new Robot(simulator,"r",coords[0]+increment,coords[1]+increment,0,0.05,0.05,color);
							//addObject(r);
						}
					}
				}
				previousColor = currentColor;
			}
		}
	}
	
	public int getSample() {
		return currentSample;
	}
	
	@Override
	public void update(double time) {
		rgb = findColorRobot(robots.get(0).getPosition().getX(),robots.get(0).getPosition().getY());
	}
	
	/**
	 * Finds the image's RGB value directly below the robot's position
	 */
	public int[] findColorRobot(double robotX, double robotY) {
		
		double[] coords = envCoordToImageCoord(robotX,robotY);
		
		return findColor(coords[0],coords[1]);
	}
	
	public double[] envCoordToImageCoord(double x, double y) {
		//de-center the coordinates
		//0,0 is no longer in the middle. It is now the top left of the image
		x+=this.width/2;
		y+=this.height/2;
		
		double scaleX = bufferedImage.getWidth()/this.width;
		double scaleY = bufferedImage.getHeight()/this.height;
		
		x*=scaleX;
		y*=scaleY;
		
		y = image.getHeight(null)-y;
		
		return new double[]{x,y};
	}
	
	/**
	 * Get the environment's X,Y coordinate based on a coordinate from the image
	 */
	public double[] imageCoordToEnvCoord(double x, double y) {
		
		double scaleX = bufferedImage.getWidth()/this.width;
		double scaleY = bufferedImage.getHeight()/this.height;
		
		x/=scaleX;
		y/=scaleY;
		
		x-=this.width/2;
		y=this.height-y;
		y-=this.height/2;
		
		return new double[]{x,y};
	}
	
	/**
	 * Get the RGB color from the image's X,Y coordinate
	 * @return an array with the RGB components (0 to 255)
	 */
	private int[] findColor(double x, double y) {
		
		
		//The default value if the coordinate is not inside the image 
		int r = 255;
		int g = 255;
		int b = 255;
		
		if(x > 0 && x < bufferedImage.getWidth() &&
				y > 0 && y < bufferedImage.getHeight())
		{
			int rgb = bufferedImage.getRGB((int)x, (int)y);
			
			r = (rgb & 0x00ff0000) >> 16;
			g = (rgb & 0x0000ff00) >> 8;
			b =  rgb & 0x000000ff;
		}
		
		return new int[]{r,g,b};
	}
	
	@Override
	public int[] getGroundColor(int robotId) {
		return rgb;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	@Override
	public void draw(Renderer renderer){
		robots.get(0).setBodyColor(Color.GRAY);
		renderer.drawCircle(new Point2d(0, 0), forbiddenArea);
		
		//if(image != null)
		//	renderer.drawImage(image);
	}
	
	public LinkedList<Square> getSquares() {
		return squares;
	}
	
	public int getSquareSize() {
		return squareSize;
	}
	
	public double getImageSize() {
		return imageSize;
	}
	
	public class Square {
		int x;
		int y;
		int distanceToFinish = 0;
		Color color;
		
		public int getDistance() {
			return distanceToFinish;
		}
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
	}
}