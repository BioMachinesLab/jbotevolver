package environment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import sensors.DistanceToBSensor;
import sensors.NearTypeBRobotSensor;
import sensors.TypeBRobotSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import utils.Cell;
import utils.Edge;
import utils.Vertex;
import controllers.RunawayController;

public class ForagingEnvironment extends OpenEnvironment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private static final String DESC_A = "type0";
	private static final String DESC_B = "type1";
	private ArrayList<Robot> typeARobots = new ArrayList<Robot>();
	private ArrayList<Robot> typeBRobots = new ArrayList<Robot>();
	
	private ArrayList<Vertex> vertex = new ArrayList<Vertex>();
	
	private boolean connected;
	
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="20")
	private int numberOfPreys;
	
	private Random random;
	private Simulator simulator;
	private Vector2d avgOrientation = new Vector2d(0,0); 
	private double rangeB;
	private int numberOfFoodSuccessfullyForaged = 0;

	public ForagingEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		
		numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
	}
	

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		this.simulator = simulator;
		random = simulator.getRandom();
		
		for(int i = 0; i < numberOfPreys; i++ ){
			addPrey(new Prey(simulator, "Prey "+i, newRandomStartPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
		
		for(Robot r: getRobots()){
			if(r.getDescription().equals(DESC_A))
				typeARobots.add(r);
			else if(r.getDescription().equals(DESC_B)) {
				typeBRobots.add(r);
				Vertex v = new Vertex(r.getId());
				vertex.add(v);
			} 
		}

		//		typeBRobots.get(0).setPosition(0, 2);
		//		typeBRobots.get(1).setPosition(0, 2.1);
	}

	@Override
	public Vector2d newRandomPosition() {		
		double radius = random.nextDouble()*(width) + width/1; //posiciona presas ente 4 e 8
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle), radius*Math.sin(angle));
	}
	
	private Vector2d newRandomStartPosition() {
		double radius = random.nextDouble()*(width-1) + width/4; //posiciona presas ente 1 e 4
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle), radius*Math.sin(angle));
	}
	
	private void updateAvgOrientation() {
		double x = 0.0;
		double y = 0.0;
		
		for(Robot r: robots) {
			if(!r.getDescription().equals("prey")){
				x += Math.cos(r.getOrientation());
				y += Math.sin(r.getOrientation());
			}
		}
		
		avgOrientation = new Vector2d(x, y);

	}

	@Override
	public void update(double time) {
		updateEnvironment();
	}

	private void updateEnvironment() {
		updateAvgOrientation();

		DistanceToBSensor sensorB = (DistanceToBSensor) typeBRobots.get(0).getSensorByType(DistanceToBSensor.class);
		rangeB = sensorB.getRange();

		ArrayList<Edge> edges = new ArrayList<Edge>();

		for(Robot b: typeBRobots) {
			for(Robot r: typeBRobots) {
				if(!r.equals(b) && r.getPosition().distanceTo(b.getPosition()) <= rangeB - r.getRadius()) {
					Edge edge = null;
					if(r.getId() < b.getId())
						edge = new Edge(new Vertex(r.getId()), new Vertex(b.getId()));
					else 
						edge = new Edge(new Vertex(b.getId()), new Vertex(r.getId()));
					if(!edges.contains(edge))
						edges.add(edge);
				}
			}
		}

		connected = true;

		for(Vertex v: vertex){
			LinkedList<Edge> vertexEdges = new LinkedList<Edge>(); 
			for(Edge edge: edges) {
				if(edge.contains(v))
					vertexEdges.add(edge);
			}
			v.setEdges(vertexEdges);
			if(vertexEdges.isEmpty())
				connected = false;
		}

		int numberOfNodes = vertex.size();
		LinkedList<Vertex> closed = new LinkedList<Vertex>();

		LinkedList<Vertex> openList = new LinkedList<Vertex>();
		openList.add(vertex.get(0));

		while(closed.size() != numberOfNodes && !openList.isEmpty() && connected){ 
			while(openList.getFirst().isClosed()) {
				openList.pop();
			}
			Vertex closingVertex = openList.pop();
			closed.add(closingVertex);

			//for each non closed vertex with an edge from closingVertex
			for(Edge edge: closingVertex.getEdges()){
				int index = vertex.indexOf(edge.getNeighbour(closingVertex));
				if(!closed.contains(vertex.get(index)))
					openList.push(vertex.get(index));
			}
		}

		for(Robot robot: typeARobots){
			if(robot.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0) != 1)
				connected = false;
		}

		if(closed.size() != numberOfNodes) 
			connected = false;


		if(numberOfPreys > 0){
			for(Robot r: typeARobots){
				for(Prey prey: simulator.getEnvironment().getPrey()){
					double distance = prey.getPosition().distanceTo(r.getPosition());
					if(distance < (r.getRadius() + 0.05)) {
						prey.teleportTo(newRandomPosition());
						if(connected)
							numberOfFoodSuccessfullyForaged++;
					}
				}

			}
		}
		
	}

	@Override
	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}
	
	@Override
	public Vector2d getAvgOrientation() {
		return avgOrientation;
	}

}
