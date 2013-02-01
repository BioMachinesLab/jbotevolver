package factories;

import java.io.Serializable;

import simulation.Simulator;
import simulation.util.Arguments;
import experiments.AggregationExperiment;
import experiments.CoevolutionExperiment;
import experiments.ColorMatchExperiment;
import experiments.Experiment;
import experiments.FoodWaterExperiment;
import experiments.ForageWithCommunicationExperiment;
import experiments.ForageWithProgrammedExperiment;
import experiments.ForageWithProgrammedRGBExperiment;
import experiments.RoomMazeBackExperiment;

public class ExperimentFactory extends Factory implements Serializable {

	public ExperimentFactory(Simulator simulator) {
		super(simulator);
	}

	public Experiment getExperiment(Arguments arguments,
			Arguments environmentArguments, Arguments robotArguments,
			Arguments controllerArguments)  {
		if (arguments == null) {
			return new Experiment(simulator, arguments, environmentArguments,
					robotArguments, controllerArguments);
		}

		if (!arguments.getArgumentIsDefined("name")) {
			return new Experiment(simulator, arguments, environmentArguments,
					robotArguments, controllerArguments);
		} else {
			String name = arguments.getArgumentAsString("name");
			if (name.equals("foragewithrgbprogrammed")) {
				return new ForageWithProgrammedRGBExperiment(simulator,
						arguments, environmentArguments, robotArguments,
						controllerArguments);
			} else if (name.equals("foragewithprogrammed")) {
				return new ForageWithProgrammedExperiment(simulator, arguments,
						environmentArguments, robotArguments,
						controllerArguments);
			} else if (name.equals("foragewithtwoteamsandtwonests")) {
				return new CoevolutionExperiment(simulator, arguments,
						environmentArguments, robotArguments,
						controllerArguments);	
			} else if (name.equals("foragewithcommunication")) {
				return new ForageWithCommunicationExperiment(simulator,
						arguments, environmentArguments, robotArguments,
						controllerArguments);
			} else if (name.equals("foodwaterexperiment")) {
				return new FoodWaterExperiment(simulator,
						arguments, environmentArguments, robotArguments,
						controllerArguments);
			} else if (name.equals("colormatch")) {
				return new ColorMatchExperiment(simulator, arguments,
						environmentArguments, robotArguments,
						controllerArguments);
			} else if (name.equals("aggregation")) {
				return new AggregationExperiment(simulator, arguments,
						environmentArguments, robotArguments,
						controllerArguments);
			} else if (name.equals("roommazeback")) {
				return new RoomMazeBackExperiment(simulator, arguments,
						environmentArguments, robotArguments,
						controllerArguments);
			} else {
				throw new RuntimeException("Unknown experiment specified: "
						+ name);
			}
		}
	}

	public CoevolutionExperiment getCoevolutionExperiment(
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotArguments, Arguments controllerArguments) {
		return new CoevolutionExperiment(simulator, experimentArguments, environmentArguments, robotArguments, controllerArguments);
	}
}
