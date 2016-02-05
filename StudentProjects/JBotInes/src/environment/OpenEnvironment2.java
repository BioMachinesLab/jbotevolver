package environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mathutils.Vector2d;
import sensors.DistanceToBSensor;
import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Line;
import simulation.robot.Robot;
import simulation.util.Arguments;
import utils.Cell;
import utils.Edge;
import utils.Vertex;
import controllers.RunawayController;

public class OpenEnvironment2 extends Environment {

	private static final String DESC_A = "type0";
	private static final String DESC_B = "type1";
	private ArrayList<Robot> typeARobots = new ArrayList<Robot>();
	private ArrayList<Robot> typeBRobots = new ArrayList<Robot>();
	private ArrayList<Robot> preyRobots = new ArrayList<Robot>();

	private int numberOfFoodSuccessfullyForaged = 0;

	private boolean preysExist;
	private boolean specialBehavior;

	private Random random;

	private double distanceB2B;
	private boolean connected;
	private Vector2d centerOfMass = new Vector2d(0,0);
	private LinkedList<Cell> grid = new LinkedList<Cell>();
	private static int CELLS = 50;

	private ArrayList<Vertex> vertex = new ArrayList<Vertex>();

	private Vector2d avgOrientation = new Vector2d(0,0);

	private boolean variablePreysPerSample;
	private boolean variableStepsPerSample;
	
	private int stepsInterval;
	private int startSteps;
	
	private int numberOfClusters;
	private List<List<Robot>> clusters;
	
	
    protected double wallsDistance = 100;
    private LinkedList<Line> lines = new LinkedList<Line>();

	public OpenEnvironment2(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		random = simulator.getRandom();

		preysExist = arguments.getArgumentAsIntOrSetDefault("preysexist", 0) == 1;
		specialBehavior = arguments.getArgumentAsIntOrSetDefault("specialbehavior", 0)==1; 
		variablePreysPerSample = arguments.getArgumentAsIntOrSetDefault("variablepreys", 0)==1;
		variableStepsPerSample = arguments.getArgumentAsIntOrSetDefault("variablesteps", 0)==1;
		wallsDistance = arguments.getArgumentAsDoubleOrSetDefault("wallsdistance", wallsDistance);
		
		if(variableStepsPerSample){
			startSteps = arguments.getArgumentAsIntOrSetDefault("startsteps", 2000);
			stepsInterval = arguments.getArgumentAsIntOrSetDefault("stepsinterval", 2000);
		}
		
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		if(getRobots().size() < 12)
			System.out.println("ENV: Create robots fail");

		for(Robot r: simulator.getRobots()){
			if(r.getDescription().equals(DESC_A))
				typeARobots.add(r);
			else if(r.getDescription().equals(DESC_B)) {
				typeBRobots.add(r);
				Vertex v = new Vertex(r.getId());
				vertex.add(v);
			}
		}
		
		DistanceToBSensor sensorB = (DistanceToBSensor) typeBRobots.get(0).getSensorByType(DistanceToBSensor.class);
		distanceB2B = sensorB.getRange();
		
		if(variableStepsPerSample){
			steps = (int)(random.nextInt(2000)+2000);
		}
		
		//geofence points
		LinkedList<Vector2d> points = new LinkedList<Vector2d>();
		
		Vector2d upperLeft = new Vector2d(-1*wallsDistance, 1*wallsDistance);
		points.add(upperLeft);
		Vector2d upperRight = new Vector2d(1*wallsDistance, 1*wallsDistance);
		points.add(upperRight);
		Vector2d lowerRight = new Vector2d(1*wallsDistance, -1*wallsDistance);
		points.add(lowerRight);
		Vector2d lowerLeft = new Vector2d(-1*wallsDistance, -1*wallsDistance);
		points.add(lowerLeft);
		
		addLines(points, simulator);

	}	

	public Vector2d newRandomPosition() {		
		double radius = random.nextDouble()*47 + 3; //posiciona presas ente 3 e 50
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle) + centerOfMass.x, radius*Math.sin(angle) + centerOfMass.y);
	}

	@Override
	public void update(double time) {
		updateConnected();
	}

	private void updateConnected() {
		clusters = getClusters(typeBRobots);
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
                            
                            if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= distanceB2B) {
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

	public Vector2d getCenterOfMass() {
		return centerOfMass;
	}

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

	public LinkedList<Cell> getGrid() {
		return grid;
	}

	public Vector2d getAvgOrientation() {
		return avgOrientation;
	}
}
