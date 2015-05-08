package evaluationfunctions;

import java.util.ArrayList;

import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction2 extends EvaluationFunction{

	public CoveredAreaEvaluationFunction2(Arguments args) {
		super(args);
	}

	private ArrayList<Robot> typeA = new ArrayList<Robot>();
	private ArrayList<Robot> typeB = new ArrayList<Robot>();

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

		for(Robot b: typeB) {
			if(b.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0) == 1) {
				for(Robot a: typeA){
					if (a.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0) == 1){

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
		//System.out.println(areaCovered + " infinity: " + infinity);

		fitness += areaCovered * 0.01;

		if(fitness == Double.POSITIVE_INFINITY)
			System.out.println("NOOO");
	}
}
