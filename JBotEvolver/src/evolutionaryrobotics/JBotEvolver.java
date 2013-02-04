package evolutionaryrobotics;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import controllers.FixedLenghtGenomeEvolvableController;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import factories.EvaluationFunctionFactory;
import factories.EvolutionFactory;
import factories.PopulationFactory;

public class JBotEvolver extends JBotSim {
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
	}
	
	public Evolution getEvolution() {
		return EvolutionFactory.getEvolution(this,getArguments().get("--evolution"));
	}
	
	public EvaluationFunction getEvaluationFunction(Simulator simulator) {
		return EvaluationFunctionFactory.getEvaluationFunction(simulator, arguments.get("--evaluation"));
	}
	
	public Population getPopulation() {
		try {
			return PopulationFactory.getPopulation(getArguments().get("--population"));
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public void setChromosome(ArrayList<Robot> robots, Chromosome chromosome) {
		for (Robot r : robots) {
			if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
				controller.setNNWeights(chromosome.getAlleles());
			}
		}	
	}
	
	public void submitTask(Callable<Object> c) {
		
	}

	public Object getResult() {
		return null;
	}
}