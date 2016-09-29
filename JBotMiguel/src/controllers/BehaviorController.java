package controllers;

import java.util.ArrayList;

import behaviors.Behavior;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class BehaviorController extends NeuralNetworkController implements FixedLenghtGenomeEvolvableController {
	
	protected boolean[] parallelController;
	protected ArrayList<Controller> subControllers = new ArrayList<Controller>();
	protected ArrayList<Controller> parallelSubControllers = new ArrayList<Controller>();
	protected int currentSubNetwork = 0;
	protected boolean keepFeeding = false;
	protected boolean resetChosen = true;
	protected 	boolean debugMax = false;
	private int switches = 0;
	
	@ArgumentsAnnotation(name="fixedoutput", defaultValue="-1")
	private int fixedOutput = -1;
	@ArgumentsAnnotation(name="printvalues", values={"0","1"})
	private boolean printValues = false;
	
	public BehaviorController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		setupControllers(simulator, args);
		fixedOutput = args.getArgumentAsIntOrSetDefault("fixedoutput", fixedOutput);
		printValues = args.getArgumentAsIntOrSetDefault("printvalues", 0) == 1;
	}
	
	@Override
	public void controlStep(double time) {
		
		if(!subControllers.isEmpty()) {
			int output = chooseOutput();
			
			if(fixedOutput >= 0)
				output = fixedOutput;
			
			boolean skip = false;
			
			if(subControllers.get(currentSubNetwork) instanceof Behavior) {
				Behavior b = (Behavior)subControllers.get(currentSubNetwork);
				skip = b.isLocked();
			}
			
			if(output != currentSubNetwork && !skip) {
				switches++;
				currentSubNetwork = output;
				if(resetChosen) {
					subControllers.get(currentSubNetwork).reset();
				}
			}
			
			neuralNetwork.controlStep(time);
			
			/*
			 * Feed these first. The chosen network should be the first to act.
			 * This will not work correctly if some of the behavior primitive networks activate
			 * some actuator that others do... I'm relying on the last controller to override the
			 * actuator values.
			*/
			if(keepFeeding) {
				for(int i = 0 ; i < subControllers.size() ; i++) {
					if(i != currentSubNetwork) {
						subControllers.get(i).controlStep(time,neuralNetwork.getOutputNeuronStates()[i]);
					}
				}
			}
			
			subControllers.get(currentSubNetwork).controlStep(time,neuralNetwork.getOutputNeuronStates()[currentSubNetwork]);
		}
		
		if(!parallelSubControllers.isEmpty())
			executeParallelNetworks(time);
		
		if(printValues) {
			for(int i = 0 ; i < neuralNetwork.getInputNeuronStates().length ; i++) {
				System.out.print(neuralNetwork.getInputNeuronStates()[i]+" ");
			}
			DifferentialDriveRobot r = (DifferentialDriveRobot)robot;
			double lw = r.getLeftWheelSpeed();
			double rw = r.getRightWheelSpeed();
			lw/=0.1;
			rw/=0.1;
			lw = Math.min(1,(lw+1.0)/2.0);
			rw = Math.min(1,(rw+1.0)/2.0);
			System.out.println(lw+" "+rw);
		}
	}
	
	private void executeParallelNetworks(double time) {
		for(int i = 0 ; i < parallelSubControllers.size(); i++)
			parallelSubControllers.get(i).controlStep(time,neuralNetwork.getOutputNeuronStates()[currentSubNetwork]);
	}
	
	private int chooseOutput() {
		
		double[] outputStates = neuralNetwork.getOutputNeuronStates();
		
		int maxIndex = -1;
		
		do{
			maxIndex++;
		} while(parallelController[maxIndex]);
		
		for(int i = maxIndex+1 ; i < outputStates.length ; i++)
			if((outputStates[i] > outputStates[maxIndex] && !parallelController[i]) || (debugMax && outputStates[i] >= outputStates[maxIndex]))
				maxIndex = i;

		return maxIndex;
	}
	
	@Override
	public void reset() {
		neuralNetwork.reset();
		for(Controller c : subControllers)
			c.reset();
	}
	
	protected void setupControllers(Simulator simulator, Arguments args) {
		
		if(args.getArgumentIsDefined("subcontrollers")) {
			Arguments subControllerArgs = new Arguments(args.getArgumentAsString("subcontrollers"));
			
			parallelController = new boolean[subControllerArgs.getNumberOfArguments()];
			
			for(int i = 0 ; i < subControllerArgs.getNumberOfArguments() ; i++) {
				
				boolean parallel = subControllerArgs.getArgumentAt(i).startsWith("_");
				parallelController[i] = parallel;
				
				Arguments currentSubControllerArgs = new Arguments(subControllerArgs.getArgumentAsString(subControllerArgs.getArgumentAt(i)));
				
				Controller c = Controller.getController(simulator, robot, currentSubControllerArgs);
				
				if(parallel) 
					parallelSubControllers.add(c);
				
				subControllers.add(c);
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
	
	public int getNumberOfSwitches() {
		return switches;
	}
	
}