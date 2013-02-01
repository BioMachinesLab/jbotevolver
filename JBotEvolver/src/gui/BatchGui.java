package gui;

import java.io.Serializable;

import simulation.Simulator;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import experiments.Experiment;
import gui.renderer.Renderer;

public class BatchGui implements Gui, Serializable {
	Experiment experiment;
		
	public BatchGui() {
	}
	
//	@Override
	public void dispose() {
	}

//	@Override
	public void run(Simulator simulator, Renderer renderer,	Experiment experiment, EvaluationFunction evaluationFunction, int maxNumberOfSteps) {

		int currentStep = 0;
		while (!experiment.hasEnded() && currentStep < maxNumberOfSteps) {			
			simulator.performOneSimulationStep(currentStep);
			if (evaluationFunction != null) {
				evaluationFunction.step();
			}	
			currentStep++;
		}
	}
}
