package factories;

import java.io.Serializable;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.AndersForageWithCommunicationEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.AverageDistanceFromCenterEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.BeeEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.CenterOfMassAndClustersEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.CenterOfMassEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.ClutteredMazeEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.CollaboratingTwoNetsForagingEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.ColorMatchEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.ColorNearPreyEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.CommunicationEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.CompetingTwoNetsForagingEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.CrossRoomsEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.ERSimbadForagingEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.FindWallButtonEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.FoodWaterEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.ForagingEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.GoToDoorEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.GroupFunction;
import evolutionaryrobotics.evaluationfunctions.InverseTMazeEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.MazeEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.OpenDoorEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.PickAndDropsEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.RoomMazeBackBehaviorsEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.RoomMazeBackEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.StayInTheNestEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.StepsWithPreyEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.TMazeEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.TwoRoomsEvaluationFunction;
import experiments.Experiment;

public class EvaluationFunctionFactory extends Factory implements Serializable {

	public EvaluationFunctionFactory(Simulator simulator) {
		super(simulator);
		// TODO Auto-generated constructor stub
	}

	public EvaluationFunction getEvaluationFunction(Arguments arguments,
			Experiment experiment) {
		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("EvaluationFunction 'name' not defined: "
					+ arguments.toString());
		}

		String evaluationFunctionName = arguments.getArgumentAsString("name");
		if (evaluationFunctionName.equalsIgnoreCase("foraging")) {
			return new ForagingEvaluationFunction(simulator);
		} else if (evaluationFunctionName.equalsIgnoreCase("stayinthenest")) {
			return new StayInTheNestEvaluationFunction(simulator);
		} else if ((evaluationFunctionName.equalsIgnoreCase("centerofmass"))) {
			return new CenterOfMassEvaluationFunction(simulator);
		} else if ((evaluationFunctionName.equalsIgnoreCase("group"))) {
			return new GroupFunction(simulator);
		} else if (evaluationFunctionName
				.equalsIgnoreCase("averagedistancefromcenter")) {
			AverageDistanceFromCenterEvaluationFunction evaluationFunction = new AverageDistanceFromCenterEvaluationFunction(
					simulator, arguments);
			if (arguments.getArgumentIsDefined("countevolvingrobotsonly")) {
				evaluationFunction.enableCountEvolvingRobotsOnly();
			}
			return evaluationFunction;

		} else if (evaluationFunctionName.equalsIgnoreCase("pickanddrop")) {
			PickAndDropsEvaluationFunction evaluationFunction = new PickAndDropsEvaluationFunction(
					simulator);
			if (arguments.getArgumentIsDefined("countevolvingrobotsonly")) {
				evaluationFunction.enableCountEvolvingRobotsOnly();
			}
			return evaluationFunction;

		} else if (evaluationFunctionName.equalsIgnoreCase("stepswithprey")) {
			StepsWithPreyEvaluationFunction evaluationFunction = new StepsWithPreyEvaluationFunction(
					simulator, arguments);
			if (arguments.getArgumentIsDefined("countevolvingrobotsonly")) {
				evaluationFunction.enableCountEvolvingRobotsOnly();
			}
			return evaluationFunction;
		} else if (evaluationFunctionName.equalsIgnoreCase("ersimbadforaging")) {
			return new ERSimbadForagingEvaluationFunction(simulator);
		} else if (evaluationFunctionName.equalsIgnoreCase("communication")) {
			return new CommunicationEvaluationFunction(simulator);
		} else if (evaluationFunctionName.equalsIgnoreCase("colornearprey")) {
			return new ColorNearPreyEvaluationFunction(simulator, arguments);
		} else if (evaluationFunctionName.equalsIgnoreCase("andersforagewithcommunication")) {
			return new AndersForageWithCommunicationEvaluationFunction(simulator, arguments);
		} else if (evaluationFunctionName.equalsIgnoreCase("competingTwoNestForaging")) {
			return new CompetingTwoNetsForagingEvaluationFunction(simulator, arguments);
		} else if (evaluationFunctionName.equalsIgnoreCase("collaboratingTwoNestForaging")) {
			return new CollaboratingTwoNetsForagingEvaluationFunction(simulator, arguments);
		} else if (evaluationFunctionName.equalsIgnoreCase("bee")) {
			return new BeeEvaluationFunction(simulator, arguments);
		} else if (evaluationFunctionName.equalsIgnoreCase("colormatch")) {
			return new ColorMatchEvaluationFunction(simulator);
		} else if(evaluationFunctionName.equalsIgnoreCase("centerofmassandclusters")){
			return new CenterOfMassAndClustersEvaluationFunction(simulator);
		}  else if(evaluationFunctionName.equalsIgnoreCase("maze")){
			return new MazeEvaluationFunction(simulator);
		}  else if(evaluationFunctionName.equalsIgnoreCase("tmaze")){
			return new TMazeEvaluationFunction(simulator,arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("foodwater")){
			return new FoodWaterEvaluationFunction(simulator);
		} else if(evaluationFunctionName.equalsIgnoreCase("clutteredmaze")){
			return new ClutteredMazeEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("inversetmaze")){
			return new InverseTMazeEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("roommazeback")){
			return new RoomMazeBackEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("roommazebackbehaviors")){
			return new RoomMazeBackBehaviorsEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("tworooms")){
			return new TwoRoomsEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("crossrooms")){
			return new CrossRoomsEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("findwallbutton")){
			return new FindWallButtonEvaluationFunction(simulator, arguments);
		}  else if(evaluationFunctionName.equalsIgnoreCase("opendoor")){
			return new OpenDoorEvaluationFunction(simulator, arguments);
		} else if(evaluationFunctionName.equalsIgnoreCase("gotodoor")){
			return new GoToDoorEvaluationFunction(simulator, arguments);
		} else {
			throw new RuntimeException("EvaluationFunction: "
					+ arguments.getArgumentIsDefined("name") + " not defined!");
		}
		
		// return null;
	}
}
