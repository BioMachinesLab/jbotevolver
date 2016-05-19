package evaluationfunctions;

import simulation.Simulator;
import simulation.util.Arguments;
import environments.LightEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class NEATTestEvaluationFunction extends EvaluationFunction{
	
	double timeToMove = 0;
	double targetTime = 0;
	double percentage = 0;
	double bonus = 0;

	public NEATTestEvaluationFunction(Arguments args) {
		super(args);
	}
	
	@Override
	public void update(Simulator simulator) {
		
		NeuralNetworkController c = (NeuralNetworkController)simulator.getRobots().get(0).getController();
		NeuralNetwork cn = c.getNeuralNetwork();
		LightEnvironment env = (LightEnvironment)simulator.getEnvironment();
		
		double[] outputs = cn.getOutputNeuronStates();
		double[] inputs = cn.getInputNeuronStates();
		double time = simulator.getTime();
		
		
		if(time == 0) {
			timeToMove = (int)(simulator.getRandom().nextDouble()*30+30);
			targetTime = timeToMove + 10;
//			System.out.println(timeToMove+" "+targetTime);
		}
		
		if(time == timeToMove) {
//			System.out.println("moved");
			env.moveLightpole(1000, 1000);
		}
		
//		if(time == targetTime)
//			System.out.println("target");
		
		percentage = time/env.getSteps();
		
		if(time < targetTime && time > 1) {
			if(outputs[0] <= 0.5) { //bad network
				simulator.stopSimulation();
			}
		}
		
		if(time >= targetTime) {
			
			if(outputs[0] <= 0.5) { //good network
				bonus+= 1;
				simulator.stopSimulation();
			} else {
//				bonus+=(1-outputs[0])/simulator.getEnvironment().getSteps();
			}
		}
		
		if(time > targetTime + 10) {
			//bad network
			simulator.stopSimulation();
		}
		
//		System.out.println(time+" "+inputs[0]+" "+" "+outputs[0]);
		fitness = percentage + bonus;
	}
}