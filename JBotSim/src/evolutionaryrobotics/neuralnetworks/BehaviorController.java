package evolutionaryrobotics.neuralnetworks;

import java.util.ArrayList;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class BehaviorController extends NeuralNetworkController implements FixedLenghtGenomeEvolvableController {
	
	private ArrayList<Controller> subControllers = new ArrayList<Controller>();
	int currentSubNetwork = 0;
	boolean keepFeeding = false;
	boolean resetChosen = true;
	boolean debugMax = false;
	
	public BehaviorController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		setupControllers(simulator, args);
	}
	
	@Override
	public void controlStep(double time) {
		int output = chooseOutput();
		
		if(output != currentSubNetwork) {
			currentSubNetwork = output;
			if(resetChosen) {
				subControllers.get(currentSubNetwork).reset();
			}
		}
		
		neuralNetwork.controlStep(time);
		
		//Feed these first. The chosen network should be the first to act.
		//This will not work correctly if some of the behavior primitive networks activate
		//some actuator that others do... I'm relying on the last controller to override the
		//actuator values.
		if(keepFeeding) {
			for(int i = 0 ; i < subControllers.size() ; i++) {
				if(i != currentSubNetwork) {
					subControllers.get(i).controlStep(time);
				}
			}
		}
		
		subControllers.get(currentSubNetwork).controlStep(time);
	}
	
	private int chooseOutput() {
		
		double[] outputStates = neuralNetwork.getOutputNeuronStates();
		
		int maxIndex = 0;
		
		for(int i = 1 ; i < outputStates.length ; i++)
			if(outputStates[i] > outputStates[maxIndex] || (debugMax && outputStates[i] >= outputStates[maxIndex]))
				maxIndex = i;
		return maxIndex;
	}
	
	@Override
	public void reset() {
		neuralNetwork.reset();
		for(Controller c : subControllers)
			c.reset();
	}
	
	private void setupControllers(Simulator simulator, Arguments args) {
		
		if(!args.getArgumentAsString("subcontrollers").isEmpty()) {
			Arguments subControllerArgs = new Arguments(args.getArgumentAsString("subcontrollers"));
			
			for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
				Arguments currentSubControllerArgs = new Arguments(subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
				subControllers.add(Controller.getController(simulator, robot, currentSubControllerArgs));
			}
		}
		
		//Setting up main Controller
		neuralNetwork = (NeuralNetwork)NeuralNetwork.getNeuralNetwork(simulator, robot, new Arguments(args.getArgumentAsString("network")));
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
		
		resetChosen = args.getArgumentAsIntOrSetDefault("resetchosen", 1) == 1;
		keepFeeding = args.getArgumentAsIntOrSetDefault("keepfeeding", 0) == 1;
		debugMax = args.getArgumentAsIntOrSetDefault("debugmax", 0) == 1;
	}
	
	public ArrayList<Controller> getSubControllers() {
		return subControllers;
	}
	
	public int getCurrentSubNetwork() {
		return currentSubNetwork;
	}
}