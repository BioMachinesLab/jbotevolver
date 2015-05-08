package evaluationfunctions;

import java.util.ArrayList;
import java.util.LinkedList;

import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import utils.Edge;
import utils.Vertex;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction4 extends EvaluationFunction{

	private ArrayList<Robot> typeA = new ArrayList<Robot>();
	private ArrayList<Robot> typeB = new ArrayList<Robot>();
	private double rangeA;
	private double rangeB;
	private double diameter;

	public CoveredAreaEvaluationFunction4(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {

		OpenEnvironment env = (OpenEnvironment) simulator.getEnvironment();
		typeA = env.getTypeARobots();
		typeB = env.getTypeBRobots();

		double xmax = - Double.MAX_VALUE;
		double xmin = Double.MAX_VALUE;
		double ymax = - Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double areaCovered = 0;
		boolean infinity = true;

		diameter = typeA.get(0).getDiameter();
		NearTypeBRobotSensor sensorA = (NearTypeBRobotSensor) typeA.get(0).getSensorByType(NearTypeBRobotSensor.class);
		rangeA = sensorA.getRange();
		NearTypeBRobotSensor sensorB = (NearTypeBRobotSensor) typeB.get(0).getSensorByType(NearTypeBRobotSensor.class);
		rangeB = sensorB.getRange();

		ArrayList<Vertex> vertex = new ArrayList<Vertex>();
		ArrayList<Edge> edges = new ArrayList<Edge>();

		ArrayList<Robot> temp = new ArrayList<Robot>();
		for(Robot b: typeB){
			temp.add(b);
			Vertex v = new Vertex(b.getId());
			vertex.add(v);
		}

		for(Robot b: typeB) {
			for(Robot r: temp) {
				if(!r.equals(b) && r.getPosition().distanceTo(b.getPosition()) < rangeB - diameter) {
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
		
		boolean connected = true;
		
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

		if(closed.size() == numberOfNodes && connected) {
			for(Robot b: typeB){
				for(Robot a: typeA){
					if (a.getPosition().distanceTo(b.getPosition()) < rangeA - diameter) {

						infinity = false;

						if (a.getPosition().x < xmin)
							xmin = a.getPosition().x;

						if (a.getPosition().y < ymin)
							ymin = a.getPosition().y;

						if (a.getPosition().x > xmax)
							xmax = a.getPosition().x;

						if (a.getPosition().y > ymax)
							ymax = a.getPosition().y;
					}
				}
			}

		}


		if(!infinity)
			areaCovered = Math.abs(xmin - xmax) * Math.abs(ymax - ymin);

		//System.out.println("Area: " + areaCovered + " " + connected);
		fitness += areaCovered * 0.01;

		if(fitness == Double.POSITIVE_INFINITY)
			System.out.println("NOOO");

		
	}
}
