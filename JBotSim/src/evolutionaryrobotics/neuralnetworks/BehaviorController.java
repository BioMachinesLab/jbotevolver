package evolutionaryrobotics.neuralnetworks;

import java.util.ArrayList;

import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import factories.ControllerFactory;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class BehaviorController extends Controller implements FixedLenghtGenomeEvolvableController {
	
	private ArrayList<Controller> subControllers = new ArrayList<Controller>();
	private NeuralNetworkController mainController;
	int currentSubNetwork = 0;
	boolean keepFeeding = false;
	boolean resetChosen = true;
	boolean master = false;
	
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
//				if(master)
//					System.out.println("Reset "+currentSubNetwork);
			}
		}
		
		mainController.controlStep(time);
		
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
		
		double[] outputStates = mainController.getNeuralNetwork().getOutputNeuronStates();
		
		int maxIndex = 0;
		
		for(int i = 1 ; i < outputStates.length ; i++)
			if(outputStates[i] > outputStates[maxIndex])
				maxIndex = i;
		
		return maxIndex;
	}
	
	@Override
	public void reset() {
		mainController.reset();
		for(Controller c : subControllers)
			c.reset();
	}
	
	private void setupControllers(Simulator simulator, Arguments args) {
		
		try {
			ControllerFactory factory = new ControllerFactory(simulator);
			
			if(!args.getArgumentAsString("subcontrollers").isEmpty()) {
				Arguments subControllerArgs = Arguments.createOrPrependArguments(null, args.getArgumentAsString("subcontrollers"));
				
				for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
					Arguments currentSubControllerArgs = Arguments.createOrPrependArguments(null, subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
					subControllers.add(factory.getController(robot, currentSubControllerArgs));
				}
			}
			
	//		for(Controller c : subControllers)
	//			if(c instanceof BehaviorController)
	//				master = true;
			
			//Setting up main Controller
			String oldName = args.getArgumentAsString("name");
			args.setArgument("name", args.getArgumentAsString("type"));
			mainController = (NeuralNetworkController)factory.getController(robot, args);
			args.setArgument("name", oldName);
			
			resetChosen = args.getArgumentAsIntOrSetDefault("resetchosen", 1) == 1;
			keepFeeding = args.getArgumentAsIntOrSetDefault("keepfeeding", 0) == 1;
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public ArrayList<Controller> getSubControllers() {
		return subControllers;
	}
	
	public int getCurrentSubNetwork() {
		return currentSubNetwork;
	}
	
	@Override
	public void setNNWeights(double[] weights) {
		this.mainController.setNNWeights(weights);
	}
}