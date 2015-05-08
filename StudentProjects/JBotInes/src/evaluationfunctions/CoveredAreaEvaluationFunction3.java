package evaluationfunctions;

import java.util.ArrayList;

import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction3 extends EvaluationFunction{

	private ArrayList<Robot> typeA = new ArrayList<Robot>();
	private ArrayList<Robot> typeB = new ArrayList<Robot>();
	private double rangeA;
	private double rangeB;
	private double diameter;

	public CoveredAreaEvaluationFunction3(Arguments args) {
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

		
		
		ArrayList<String> links = new ArrayList<String>();
		ArrayList<Integer> nodes = new ArrayList<Integer>();

		diameter = typeA.get(0).getDiameter();
		NearTypeBRobotSensor sensorA = (NearTypeBRobotSensor) typeA.get(0).getSensorByType(NearTypeBRobotSensor.class);
		rangeA = sensorA.getRange();
		NearTypeBRobotSensor sensorB = (NearTypeBRobotSensor) typeB.get(0).getSensorByType(NearTypeBRobotSensor.class);
		rangeB = sensorB.getRange();

		ArrayList<Robot> temp = new ArrayList<Robot>();
		for(Robot b: typeB){
			temp.add(b);
		}

		for(Robot b: typeB) {
			for(Robot r: temp) {
				if(!r.equals(b) && r.getPosition().distanceTo(b.getPosition()) < rangeB - diameter) {
					if(!nodes.contains(r.getId()) || !nodes.contains(b.getId())) {
						
						if(!nodes.contains(r.getId()))
							nodes.add(r.getId());
						
						if(!nodes.contains(b.getId()))
							nodes.add(b.getId());
						
						String link = "";
						if(r.getId() < b.getId()) 
							link = "" + r.getId() + b.getId();
						else 
							link = "" + b.getId() + r.getId();
						if(!links.contains(link))
							links.add(link);
					}
				}
			}
		}

		if(nodes.size() == typeB.size() && links.size() >= typeB.size() - 1){
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

		fitness += areaCovered * 0.01;

		if(fitness == Double.POSITIVE_INFINITY)
			System.out.println("NOOO");
	}
}
