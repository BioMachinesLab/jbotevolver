package experiments;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FoodWaterExperiment extends Experiment {

	public static final String BORN_TIME = "b";
	public static String FOOD = "f";
	public static String WATER = "w";
	public static String RECHARGED = "r";
	
	public FoodWaterExperiment(Simulator simulator,
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotsArguments, Arguments controllersArguments) {
		super(simulator, experimentArguments, environmentArguments,
				robotsArguments, controllersArguments);
		for(Robot robot: getRobots()){
			robot.setParameter(FOOD, new Double(1));
			robot.setParameter(WATER, new Double(1));
			robot.setParameter(RECHARGED, new Integer(0));
			robot.setParameter(BORN_TIME, new Integer(0));
		}

	}

	

}
