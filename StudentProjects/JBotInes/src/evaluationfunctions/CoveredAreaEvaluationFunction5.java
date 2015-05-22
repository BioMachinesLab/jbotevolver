package evaluationfunctions;

import java.util.ArrayList;
import java.util.LinkedList;

import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import utils.Cell;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction5 extends EvaluationFunction{

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

	public CoveredAreaEvaluationFunction5(Arguments args) {
		super(args);
		explore_percentage = args.getArgumentIsDefined("explorepercentage") ? args.getArgumentAsDouble("explorepercentage") : 0.001;
		prey_percentage = args.getArgumentIsDefined("preypercentage") ? args.getArgumentAsDouble("preypercentage") : 0.001;
		area_percentage = args.getArgumentIsDefined("areapercentage") ? args.getArgumentAsDouble("areapercentage") : 0.001;
		cell_width = args.getArgumentIsDefined("cellwidth") ? args.getArgumentAsInt("cellwidth") : 1;
		preyarea = args.getArgumentAsIntOrSetDefault("preyarea", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {

		env = (OpenEnvironment) simulator.getEnvironment();
		typeA = env.getTypeARobots();
		typeB = env.getTypeBRobots();
		preys = env.getPreyRobots();

		grid = env.getGrid();

		double xmax = - Double.MAX_VALUE;
		double xmin = Double.MAX_VALUE;
		double ymax = - Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double areaCovered = 0;
		boolean infinity = true;

		NearTypeBRobotSensor sensorA = (NearTypeBRobotSensor) typeA.get(0).getSensorByType(NearTypeBRobotSensor.class);
		rangeA = sensorA.getRange();



		double preyFitness = 0.0;

		if(env.isConnected()) {


			for(Robot b: typeB){
				for(Robot a: typeA){
					if (a.getPosition().distanceTo(b.getPosition()) <= rangeA - a.getDiameter()) {

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

			Cell upLeft = new Cell((int)(xmin/cell_width),(int)(ymax/cell_width));
			Cell downLeft = new Cell((int)(xmin/cell_width),(int)(ymin/cell_width));
			Cell downRight = new Cell((int)(xmax/cell_width),(int)(ymin/cell_width));

			for(int i = downLeft.getY(); i < upLeft.getY()+1; i++){
				for(int j = downLeft.getX(); j < downRight.getX(); j++){
					Cell c = new Cell(j, i);
					int index = grid.indexOf(c);
					if(index != -1 && !grid.get(index).isVisited()){
						grid.get(index).setVisited(true);
						exploreFitness++;
					}
				}
			}
			//System.out.println(exploreFitness);


			for(Robot r: simulator.getEnvironment().getRobots()){

				if((r.getDescription().equals("type0") && r.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0)==1)) {

					
//					if((r.getDescription().equals("type0") && r.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0)==1)
//							|| r.getDescription().equals("type1")) {
					
					//explore fitness
					//						Cell c = new Cell((int)(r.getPosition().x/cell_width), (int)(r.getPosition().y/cell_width));
					//						if(grid.contains(ca)){
					//							int index = grid.indexOf(c);
					//							if(!grid.get(index).isVisited()){
					//								grid.get(index).setVisited(true);
					//								exploreFitness++;
					//							}
					//						}


					//prey fitness
					for(Robot prey: preys){
						double distance = prey.getPosition().distanceTo(r.getPosition());
						if(distance < 1)
							preyFitness += 1/distance;
					}
				} 

			}

		}

		if(!infinity)
			areaCovered = Math.abs(xmin - xmax) * Math.abs(ymax - ymin);
		
		if(preyarea && simulator.getTime() != 0){
			totalAreaFitness += areaCovered * area_percentage;
			fitness += (areaCovered * area_percentage / simulator.getTime()) + preyFitness * prey_percentage;
			//System.out.println(areaCovered * area_percentage / simulator.getTime());
		} else		
			fitness += areaCovered * area_percentage + preyFitness * prey_percentage;
			
			
		//		if(fitness == Double.POSITIVE_INFINITY)
		//			System.out.println("NOOO");

		//loose ends :
		//	*prey fitness x50/100/10???
		//	*immediate neighbors
		//	*get preys while connected; area is irrelevant -> covarea6


		//System.out.println("Area fitness: " + (areaCovered * area_percentage));
		//System.out.println("Prey fitness: " + (preyFitness * prey_percentage));
		//System.out.println("Numb of cells explored: " + exploreFitness);
		//System.out.println("Total this step: " + ((areaCovered*area_percentage)+(preyFitness * prey_percentage)));
		//System.out.println("Total: " + fitness);
		
		//neural network weights
	}

	@Override
	public double getFitness() {
		if(preyarea)
			return fitness + env.getNumberOfFoodSuccessfullyForaged()*(totalAreaFitness/env.getSteps()) + exploreFitness*explore_percentage;
		else
			return fitness + env.getNumberOfFoodSuccessfullyForaged()*50 + exploreFitness*explore_percentage;
	}


}
