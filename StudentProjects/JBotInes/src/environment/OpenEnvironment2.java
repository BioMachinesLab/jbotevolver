package environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mathutils.Vector2d;
import sensors.DistanceToBSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Line;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenEnvironment2 extends Environment {

	private static final String DESC_A = "type0";
	private static final String DESC_B = "type1";
	private ArrayList<Robot> typeARobots = new ArrayList<Robot>();
	private ArrayList<Robot> typeBRobots = new ArrayList<Robot>();
	private double shortRange = 0.0;
	private double longRange = 0.0;
	private ArrayList<Robot> preyRobots = new ArrayList<Robot>();
	private int numberOfFoodSuccessfullyForaged = 0;
	private boolean preysExist;
	private boolean specialBehavior;
	private Random random;

	private boolean connected;
//	private Vector2d centerOfMass = new Vector2d(0,0);

	private boolean variablePreysPerSample;
	private boolean variableStepsPerSample;
	private int stepsInterval;
	private int startSteps;

	private int numberOfClusters;
	private List<List<Robot>> clusters;


	protected double wallsDistanceH = 25;
	protected double wallsDistanceW = 50;
//	private LinkedList<Line> lines = new LinkedList<Line>();
	private Simulator simulator;

	private boolean obstacles = false;
	private LinkedList<Vector2d> obstacleCoordinates = new LinkedList<Vector2d>();
	private int numberOfObstacles = 5;
	private int obstacleWidth = 5;
	private int obstacleHeight = 5;

	public OpenEnvironment2(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;

		random = simulator.getRandom();

		preysExist = arguments.getArgumentAsIntOrSetDefault("preysexist", 0) == 1;
		specialBehavior = arguments.getArgumentAsIntOrSetDefault("specialbehavior", 0)==1; 
		variablePreysPerSample = arguments.getArgumentAsIntOrSetDefault("variablepreys", 0)==1;
		variableStepsPerSample = arguments.getArgumentAsIntOrSetDefault("variablesteps", 0)==1;
		wallsDistanceH = arguments.getArgumentAsDoubleOrSetDefault("wallsdistance", wallsDistanceH);
		wallsDistanceW = arguments.getArgumentAsDoubleOrSetDefault("wallswidth", wallsDistanceW);
		obstacles = arguments.getArgumentAsIntOrSetDefault("obstacles", 0) ==1;

		if(variableStepsPerSample){
			startSteps = arguments.getArgumentAsIntOrSetDefault("startsteps", 2000);
			stepsInterval = arguments.getArgumentAsIntOrSetDefault("stepsinterval", 2000);
		}

		width = wallsDistanceW*2;
		height = wallsDistanceH*2;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);

		for(Robot r: simulator.getRobots()){
			if(r.getDescription().equals(DESC_A))
				typeARobots.add(r);
			else if(r.getDescription().equals(DESC_B)) {
				typeBRobots.add(r);
			}
		}
		
		shortRange = ((DistanceToBSensor) typeARobots.get(0).getSensorByType(DistanceToBSensor.class)).getRange();
		longRange = ((DistanceToBSensor) typeBRobots.get(0).getSensorByType(DistanceToBSensor.class)).getRange();

		if(variableStepsPerSample){
			steps = (int)(random.nextInt(2000)+2000);
		}

		//geofence points
		LinkedList<Vector2d> points = new LinkedList<Vector2d>();

		Vector2d upperLeft = new Vector2d(-1*wallsDistanceW, 1*wallsDistanceH);
		points.add(upperLeft);
		Vector2d upperRight = new Vector2d(1*wallsDistanceW, 1*wallsDistanceH);
		points.add(upperRight);
		Vector2d lowerRight = new Vector2d(1*wallsDistanceW, -1*wallsDistanceH);
		points.add(lowerRight);
		Vector2d lowerLeft = new Vector2d(-1*wallsDistanceW, -1*wallsDistanceH);
		points.add(lowerLeft);

		addLines(points, simulator);

		if(obstacles){

			for(int i = 0; i < numberOfObstacles; i++){

				Vector2d startCoordinates = generateCoordinates();

				boolean valid = false;

				while(!valid){
					if(obstacleCoordinates.isEmpty()){
						//melhorar isto contemplando esta restrição no generateCoordinates em vez de dps verificar
						if(startCoordinates.x + obstacleWidth > width/2 || startCoordinates.y - obstacleHeight < -height/2){ 
							valid = false;
							startCoordinates = generateCoordinates();
					} else
							valid = true;
					} else {
						boolean fail = false;

						if(startCoordinates.x + obstacleWidth > width/2 || startCoordinates.y - obstacleHeight < -height/2)
							fail = true;
						else {
							for(Vector2d coord: obstacleCoordinates){
								if(startCoordinates.x < coord.x + obstacleWidth && startCoordinates.x > coord.x - obstacleWidth)
									fail = true;
								else {
									if(startCoordinates.y < coord.y + obstacleHeight && startCoordinates.y > coord.y - obstacleHeight)
										fail = true;
								}
							}
						}
						if(!fail)
							valid = true;
						else
							startCoordinates = generateCoordinates();
					}
				}

				obstacleCoordinates.add(startCoordinates);
				LinkedList<Vector2d> obstaclePoints = new LinkedList<Vector2d>();

				Vector2d uL = startCoordinates;
				obstaclePoints.add(uL);
				Vector2d uR = new Vector2d(startCoordinates.x+obstacleWidth, startCoordinates.y);
				obstaclePoints.add(uR);
				Vector2d lR = new Vector2d(startCoordinates.x+obstacleWidth, startCoordinates.y-obstacleHeight);
				obstaclePoints.add(lR);
				Vector2d lL = new Vector2d(startCoordinates.x,startCoordinates.y-obstacleHeight);
				obstaclePoints.add(lL);

				addLines(obstaclePoints, simulator);

			}

		}

	}	

//	public Vector2d newRandomPosition() {		
//		double radius = random.nextDouble()*47 + 3; //posiciona presas ente 3 e 50
//		double angle = random.nextDouble()*2*Math.PI;
//		return new Vector2d(radius*Math.cos(angle) + centerOfMass.x, radius*Math.sin(angle) + centerOfMass.y);
//	}

	@Override
	public void update(double time) {
		updateConnected();
	}

	private void updateConnected() {
		clusters = getClusters(typeBRobots); //only counts clusters of long range robots
		numberOfClusters = clusters.size();
		connected = numberOfClusters==1;

	}

	// bottom-up single-linked clustering
	public List<List<Robot>> getClusters(Collection<Robot> robots) {
		List<List<Robot>> clusters = new ArrayList<>();
		// One cluster for each robot
		for (Robot r : robots) {
			List<Robot> cluster = new ArrayList<>();
			cluster.add(r);
			clusters.add(cluster);
		}

		boolean merged = true;
		// stop when the clusters cannot be merged anymore

		while(merged) {
			merged = false;
			// find two existing clusters to merge
			for (int i = 0; i < clusters.size() && !merged; i++) {
				for (int j = i + 1; j < clusters.size() && !merged; j++) {
					List<Robot> c1 = clusters.get(i);
					List<Robot> c2 = clusters.get(j);
					// check if the two clusters have (at least) one individual close to the other
					for (int ri = 0; ri < c1.size() && !merged; ri++) {
						for (int rj = 0; rj < c2.size() && !merged; rj++) {
							Robot r1 = c1.get(ri);
							Robot r2 = c2.get(rj);

							if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= longRange) {
								// do the merge
								merged = true;
								clusters.get(i).addAll(clusters.get(j));
								clusters.remove(j);
							}
						}
					}
				}
			}
		}
		return clusters;
	}


	protected void addLines(LinkedList<Vector2d> waypoints, Simulator simulator) {

		for (int i = 1; i < waypoints.size(); i++) {

			Vector2d va = waypoints.get(i - 1);
			Vector2d vb = waypoints.get(i);

			simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator, "line" + i, va.getX(), va.getY(), vb.getX(), vb.getY());
			addObject(l);
		}

		Vector2d va = waypoints.get(waypoints.size() - 1);
		Vector2d vb = waypoints.get(0);

		Line l = new Line(simulator, "line0", va.getX(), va.getY(), vb.getX(), vb.getY());
		addObject(l);
	}

	private Vector2d generateCoordinates(){
		double x = (random.nextDouble()*width)-(width/2);
		double y = (random.nextDouble()*height)-(height/2);
		return new Vector2d(x,y);
	}

//	public Vector2d getCenterOfMass() {
//		return centerOfMass;
//	}

	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public ArrayList<Robot> getTypeARobots() {
		return typeARobots;
	}

	public ArrayList<Robot> getTypeBRobots() {
		return typeBRobots;
	}

	public ArrayList<Robot> getPreyRobots() {
		return preyRobots;
	}

	public boolean isConnected() {
		return connected;
	}

	public int getNumberOfClusters() {
		return numberOfClusters;
	}

	public Simulator getSimulator(){
		return simulator;
	}
}
