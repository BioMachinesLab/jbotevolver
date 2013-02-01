package experiments;

import java.util.LinkedList;
import java.util.Vector;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;


/**
 * Aggregation Experiment
 * @author Borga
 *
 */
public class AggregationExperiment extends Experiment {

	public AggregationExperiment(Simulator simulator, Arguments experimentArguments,
			Arguments environmentArguments, Arguments robotArguments,
			Arguments controllerArguments) {
		super(simulator, experimentArguments, environmentArguments, robotArguments,
				controllerArguments);
		 
	}
	
	
	/**
	 * Create robots. The number of robots can vary in each sample
	 */
	public LinkedList<Robot> createRobots() {
		int numSamples=experimentArguments.getArgumentAsInt("fitnesssample");
		int numRobots = experimentArguments.getArgumentAsInt("numberofrobots");
		String varyNumberRobots = experimentArguments.getArgumentAsString("varynumberofrobots");
		robots = super.createRobots();
		if(varyNumberRobots.equalsIgnoreCase("true")){
			if (experimentArguments != null) {
				numSamples = experimentArguments.getArgumentAsInt("fitnesssample");
			for (int i = 0; i < (numSamples%3)*numRobots; i++)
				robots.add(createOneRobot(robotArguments,controllerArguments));
			}
		}	
		//System.out.println("Robots "+robots.size());
		return robots;
	}	
}
