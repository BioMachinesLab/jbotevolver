package evaluationfunctions;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mathutils.Vector2d;
import sensors.DistanceToBSensor;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.OpenEnvironment2;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PatrollingEvaluationFunction extends EvaluationFunction{

	private OpenEnvironment2 env;
	private int numberOfSteps=0;
	private Double timestep= 0.0;
	private int drawImage = 0;
    private double radiusType0 = 0.0;
//	private double radiusType1= 0.0;
	private double penalty = 0;

	private int width; //default is 200
	private int height;
	private double decrease = 0; //0.001 =>1000 to go from 1.0 to 0.0
	private double[][] grid; 
	private double accum = 0;
	private double max = 200*200/2; //for now, but not accurate
	private int clustersSum = 0;
	private int power = 1;
	
	private double shortRange = 0.0;
	private double longRange = 0.0;

	public PatrollingEvaluationFunction(Arguments args) {
		super(args);
		drawImage = args.getArgumentIsDefined("drawimage") ? args.getArgumentAsInt("drawimage") : 0;
		width = args.getArgumentIsDefined("width") ? args.getArgumentAsInt("width") : 200;
		height = args.getArgumentIsDefined("height") ? args.getArgumentAsInt("height") : 200;
		grid = new double[height][width];
		penalty = args.getArgumentIsDefined("penalty") ? args.getArgumentAsDouble("penalty") : 0;
		decrease = args.getArgumentIsDefined("decay") ? args.getArgumentAsDouble("decay") : 0;
		//power = args.getArgumentIsDefined("power") ? args.getArgumentAsInt("power") : 1;

		//map = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		//map.getGraphics().setColor(Color.WHITE);
	}

	@Override
	public void update(Simulator simulator) {
		timestep = simulator.getTime();
		env = (OpenEnvironment2) simulator.getEnvironment();
		numberOfSteps = env.getSteps();

		if(timestep == 0){
			// sensory range = 1/2 communication range
//			radiusType0 = ((NearTypeBRobotSensor) env.getTypeARobots().get(0).getSensorByType(NearTypeBRobotSensor.class)).getRange() / 4;
//			radiusType1 = ((NearTypeBRobotSensor) env.getTypeBRobots().get(0).getSensorByType(NearTypeBRobotSensor.class)).getRange() / 4;
			radiusType0 = 2;
//			max = 2* Math.PI * (Math.pow((int)(radiusType0), 2)) * 9 * 12000;
			shortRange = ((DistanceToBSensor) env.getTypeARobots().get(0).getSensorByType(DistanceToBSensor.class)).getRange();
			longRange = ((DistanceToBSensor) env.getTypeBRobots().get(0).getSensorByType(DistanceToBSensor.class)).getRange();
		}

		if(timestep % 10 == 0){
			clustersSum   += env.getNumberOfClusters();
		}
		
		//mapping robots coordinates to image coordinates: (0,0) = (99,99)
		int factor = 1; // 1 meter = 1 px

		int center = grid.length/2;
		
		for (Robot r : env.getRobots()) {
			if(r.getDescription().equals("type1")){
				continue;
			}
			
			if(r.getDescription().equals("type0")){
				if(!robotWithinRangeOfB(r)){
					if(timestep % 10 == 0)	
						clustersSum++;
					continue;
				}
			}
			
			if(insideLines(r.getPosition(), simulator)) {

				double radius = radiusType0;

				Vector2d upperLeftCorner = toUpperLeftCorner(r.getPosition(), radius);
				Vector2d ulcScaled = toPixels(upperLeftCorner, factor, center);

				int minX = (int)(Math.round(ulcScaled.x));
				int minY = (int)(Math.round(ulcScaled.y));
				int width = (int)(radius*factor*2);
				int height = (int)(radius*factor*2);
				int maxX = minX + width;
				int maxY = minY + height;

				//drawing circles
				for (int y = minY; y < maxY; y++) {
					
					if (y >= grid.length || y < 0) {
						continue;
					}

					for (int x = minX; x < maxX; x++) {

						if (x >= grid[y].length || x < 0) {
							continue;
						}

						if (grid[y][x] == -1) {
							continue;
						}
						grid[y][x] = 1.0;
					}
				}
			} else {
//				System.out.println("Outside Lines Robot" + r.getId());
			}
		}
		
	}

	@Override
	public double getFitness() {

				if(timestep == numberOfSteps - 1){
					
					double sum = 0;
					
					for (int y = 0; y < grid.length; y++) {
						for (int x = 0; x < grid[y].length; x++) {

							//calculate sum 
							if (grid[y][x] > 0) {
								sum += grid[y][x];
							}

							//applying decrease
//							if (grid[y][x] > 0) {
//								if (grid[y][x] <= 1) {
//									grid[y][x] -= decrease;
//									if (grid[y][x] < 0) {
//										grid[y][x] = 0;
//									}
//								}
//							}
						}
					}
					
					fitness = sum / (clustersSum / (env.getSteps()/10.0)) / numberOfSteps;
		
					if(drawImage == 1){
						
						System.out.println("Grid sum: " + sum + " Cluster sum: " + (clustersSum / (env.getSteps()/10.0)) + " Fitness: "+ fitness);
						//System.out.println("Number of robots " + env.getTypeBRobots().size()+ "B " + env.getTypeARobots().size() + "A");
	
						BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						
						for (int y = 0; y < grid.length; y++) {
							for (int x = 0; x < grid[y].length; x++) {
								int color = (int) Math.round(grid[y][x]*255);
								//System.out.println("Color " + grid[y][x]);
								img.setRGB(x, y, new Color(color,color,color).getRGB());
							}
						}
						JFrame frame = new JFrame();
						//map.getGraphics().drawLine(10, 10, 60, 10);
						frame.setSize(300,300);
				
						frame.getContentPane().add(new JLabel(new ImageIcon(img)));
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.setVisible(true);	
						
						//printGrid();
					}
				}

		//			System.out.println("Fitness: " + fitness);
		//			System.out.println("Penalty: " + penalty);
		//			System.out.println((fitness-penalty)/10000);

		return fitness;
	}

	public boolean insideLines(Vector2d v, Simulator sim) {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;

		for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			if (p.getType() == PhysicalObjectType.LINE) {
				Line l = (Line) p;
				if (l.intersectsWithLineSegment(v, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
					count++;
				}
			}
		}
		return count % 2 != 0;
	}

	private void printGrid() {
		System.out.println();
		System.out.println();
		for(int y = grid.length-1 ; y >= 0 ; y--) {
			for(int x = 0 ; x < grid[y].length ; x++) {
				if(grid[y][x] == -1) {
					System.out.print(" ");
				} else if(grid[y][x] == 0) {
					System.out.print("_");
				} else if(grid[y][x] == 1) {
					System.out.print("#");
				} else {
					System.out.print("<");
				}
			}
			System.out.println();
		}
	}


	private Vector2d toUpperLeftCorner(Vector2d center, double radius){
		return new Vector2d(center.x - radius, center.y + radius);
	}

	private Vector2d toPixels(Vector2d position, int factor, int center){
		return new Vector2d(center + (position.x*factor), center - (position.y*factor));
	}
	
	private boolean robotWithinRangeOfB(Robot a){
			double range = 0.0;
			if(a.getDescription().equals("type0"))
				range = shortRange;
			else{
				if(a.getDescription().equals("type1"))
					range = longRange;
				else
					System.out.println("Something wrong with robot description -- Id: " + a.getId() + " Description: " + a.getDescription());
			}
			
			ArrayList<Robot> longRangeRobots = env.getTypeBRobots();			
			boolean value = false;
			
			for(Robot b : longRangeRobots) { 
							
				if(b.getPosition().distanceTo(a.getPosition()) < range && a.getId() != b.getId()) {
					value = true;
					break;
				}
			}
			
			return value;
	}

}
