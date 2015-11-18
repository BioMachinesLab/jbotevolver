package evaluationfunctions;

import java.util.ArrayList;
import java.util.LinkedList;

import sensors.DistanceToBSensor;
import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import utils.Cell;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction8 extends EvaluationFunction{

	private ArrayList<Robot> typeA = new ArrayList<Robot>();
	private ArrayList<Robot> typeB = new ArrayList<Robot>();
	private ArrayList<Robot> preys = new ArrayList<Robot>();

	private OpenEnvironment env;
	private double distance_percentage;
	private double forage_percentage;

	public CoveredAreaEvaluationFunction8(Arguments args) {
		super(args);
		distance_percentage = args.getArgumentIsDefined("distancepercentage") ? args.getArgumentAsDouble("distancepercentage") : 0.001;
		forage_percentage = args.getArgumentIsDefined("foragepercentage") ? args.getArgumentAsDouble("foragepercentage") : 1;
	}

	
	@Override
	public void update(Simulator simulator) {
		env = (OpenEnvironment) simulator.getEnvironment();
		preys = env.getPreyRobots();
	}
	
	@Override
	public double getFitness() {
		return ((fitness + env.getNumberOfFoodSuccessfullyForaged()*forage_percentage)/preys.size())*6/(env.getTypeARobots().size()+env.getTypeBRobots().size()); //before it was 3
	
		//bonus * 6 (minimum numb of robots) / total numb of robots
	}

}
