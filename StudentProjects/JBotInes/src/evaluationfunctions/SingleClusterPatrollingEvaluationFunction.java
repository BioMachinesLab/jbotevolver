package evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;
import environment.SquareAreaEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SingleClusterPatrollingEvaluationFunction extends EvaluationFunction{

	private SquareAreaEnvironment env;
	private int numberOfSteps=0;
	private Double timestep= 0.0;
	private int drawImage = 0;

	private int width;
	private int height;
	private double decrease = 0; //0.001 =>1000 to go from 1.0 to 0.0
	private double[][] grid; 
	private int clustersSum = 0;
	private int power = 1;
	private double radius = 1;

	public SingleClusterPatrollingEvaluationFunction(Arguments args) {
		super(args);
		drawImage = args.getArgumentIsDefined("drawimage") ? args.getArgumentAsInt("drawimage") : 0;
		width = args.getArgumentIsDefined("width") ? args.getArgumentAsInt("width") : 200;
		height = args.getArgumentIsDefined("height") ? args.getArgumentAsInt("height") : 200;
		grid = new double[height][width];
		decrease = args.getArgumentIsDefined("decay") ? args.getArgumentAsDouble("decay") : 0;
		power = args.getArgumentIsDefined("power") ? args.getArgumentAsInt("power") : power;

		//map = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		//map.getGraphics().setColor(Color.WHITE);
	}

	@Override
	public void update(Simulator simulator) {
		timestep = simulator.getTime();

		if(timestep == 0){
			env = (SquareAreaEnvironment) simulator.getEnvironment();
			numberOfSteps = env.getSteps();
			radius = ((RobotSensor) env.getRobots().get(0).getSensorByType(RobotSensor.class)).getRange() / 2;
		}

		if(timestep % 10 == 0){
			clustersSum   += env.getNumberOfClusters();
		}

		//mapping robots coordinates to image coordinates: (0,0) = (99,99)
		int factor = 1; // 1 meter = 1 px

		int center = grid.length/2;

		for (Robot r : env.getRobots()) {
			if (insideLines(r.getPosition(), simulator)) {

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
					//					if (grid[y][x] > 0) {
					//						if (grid[y][x] <= 1) {
					//							grid[y][x] -= decrease;
					//							if (grid[y][x] < 0) {
					//								grid[y][x] = 0;
					//							}
					//						}
					//					}
				}
			}

			fitness = sum / Math.pow((clustersSum/(numberOfSteps/10)), power) /1000;

//			if(drawImage == 1){
//
//				System.out.println(clustersSum);
//
//				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//				for (int y = 0; y < grid.length; y++) {
//					for (int x = 0; x < grid[y].length; x++) {
//						int color = (int) Math.round(grid[y][x]*255);
//						//System.out.println("Color " + grid[y][x]);
//						img.setRGB(x, y, new Color(color,color,color).getRGB());
//					}
//				}
//				JFrame frame = new JFrame();
//				//map.getGraphics().drawLine(10, 10, 60, 10);
//				frame.getContentPane().add(new JLabel(new ImageIcon(img)));
//				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//				frame.setVisible(true);	
//
//				//printGrid();
//			}
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

}
