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

public class CoveredAreaEvaluationFunction7 extends EvaluationFunction{

	private int cell_width  = 1;
	private double explore_percentage;
	private double prey_percentage;
	private double area_percentage;
	private boolean preyarea;

	private ArrayList<Robot> typeA = new ArrayList<Robot>();
	private ArrayList<Robot> typeB = new ArrayList<Robot>();
	private ArrayList<Robot> preys = new ArrayList<Robot>();
	private double rangeA;

	private OpenEnvironment env;
	private LinkedList<Cell> grid;
	private int exploreFitness = 0;
	private int totalAreaFitness;
	private double distance_percentage;
	private double forage_percentage;

	public CoveredAreaEvaluationFunction7(Arguments args) {
		super(args);
		explore_percentage = args.getArgumentIsDefined("explorepercentage") ? args.getArgumentAsDouble("explorepercentage") : 0.001;
		prey_percentage = args.getArgumentIsDefined("preypercentage") ? args.getArgumentAsDouble("preypercentage") : 0.001;
		area_percentage = args.getArgumentIsDefined("areapercentage") ? args.getArgumentAsDouble("areapercentage") : 0.001;
		distance_percentage = args.getArgumentIsDefined("distancepercentage") ? args.getArgumentAsDouble("distancepercentage") : 0.001;
		cell_width = args.getArgumentIsDefined("cellwidth") ? args.getArgumentAsInt("cellwidth") : 1;
		preyarea = args.getArgumentAsIntOrSetDefault("preyarea", 0) == 1;
		forage_percentage = args.getArgumentIsDefined("foragepercentage") ? args.getArgumentAsDouble("foragepercentage") : 3;
	}

	@Override
	public void update(Simulator simulator) {

		env = (OpenEnvironment) simulator.getEnvironment();
		typeA = env.getTypeARobots();
		typeB = env.getTypeBRobots();
		preys = env.getPreyRobots();

		NearTypeBRobotSensor sensorA = (NearTypeBRobotSensor) typeA.get(0).getSensorByType(NearTypeBRobotSensor.class);
		rangeA = sensorA.getRange();

		double distanceFitness = 0.0;

		if(env.isConnected()) {

			for(Robot r: typeB){
				for(Robot robot: env.getRobots()){
					if(robot.getDescription().equals("type1") && !robot.equals(r)){
						double distance = robot.getPosition().distanceTo(r.getPosition());
						if(distance < 1.8) {
							distanceFitness += distance/1.8;
						}
						
					}
				}
			}
			
			fitness += distanceFitness*distance_percentage;

		}
		
	}

	@Override
	public double getFitness() {
		return fitness + env.getNumberOfFoodSuccessfullyForaged()*forage_percentage; //before it was 3
	}


}
