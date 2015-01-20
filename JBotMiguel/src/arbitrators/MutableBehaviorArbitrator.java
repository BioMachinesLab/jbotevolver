package arbitrators;

import java.util.LinkedList;
import neat.layerered.ANNNeuron;
import neat.parameters.ParameterNeuralNetwork;
import neat.parameters.ParameterNeuron;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.BehaviorController;
import controllers.Controller;
import controllers.ParameterController;
import controllers.ParameterLocomotionController;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

public class MutableBehaviorArbitrator extends BehaviorController {
	
	private boolean configured = false;
	private Simulator sim;
	private LinkedList<Controller> configControllers; 

	public MutableBehaviorArbitrator(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		this.sim = simulator;
	}
	
	@Override
	public void controlStep(double time) {
		if(!configured) {
			ParameterNeuralNetwork net = (ParameterNeuralNetwork)neuralNetwork;
			//Setting up the sub-controllers
			
			for(ANNNeuron n : net.getNetwork().getAllNeurons()) {
				if(n instanceof ParameterNeuron) {
					double p = ((ParameterNeuron)n).getParameter();
					ParameterController c;
					if(configControllers == null) {
						c = new ParameterLocomotionController(sim, robot, new Arguments(""));
					} else {
						c = new ParameterController(sim, robot, new Arguments(""));
						c.setSubControllers(configControllers);
					}
					c.setParameter(p);
					subControllers.add(c);
				}
			}
			
			parallelController = new boolean[subControllers.size()];
			
			configured = true;
		}
		super.controlStep(time);
	}
	
	@Override
	protected void setupControllers(Simulator simulator, Arguments args) {
		
		//Setting up main controller
		neuralNetwork = (NeuralNetwork)NeuralNetwork.getNeuralNetwork(simulator, robot, new Arguments(args.getArgumentAsString("network")));
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
		
		if(!(neuralNetwork instanceof ParameterNeuralNetwork))
			throw new RuntimeException("This controller is not ready for any other type of network!");
		
		
		if(args.getArgumentIsDefined("subcontrollers")) {
			configControllers = new LinkedList<Controller>();
			Arguments subControllerArgs = new Arguments(args.getArgumentAsString("subcontrollers"));
			
//			parallelController = new boolean[subControllerArgs.getNumberOfArguments()];
			
			for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
				
//				boolean parallel = subControllerArgs.getArgumentAt(i).startsWith("_");
//				parallelController[i] = parallel;
				
				Arguments currentSubControllerArgs = new Arguments(subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
				
				Controller c = Controller.getController(simulator, robot, currentSubControllerArgs);
				
//				if(parallel) 
//					parallelSubControllers.add(c);
				
				configControllers.add(c);
			}
		}
		
		resetChosen = args.getArgumentAsIntOrSetDefault("resetchosen", 1) == 1;
		keepFeeding = args.getArgumentAsIntOrSetDefault("keepfeeding", 0) == 1;
		debugMax = args.getArgumentAsIntOrSetDefault("debugmax", 0) == 1;
	}	
}
