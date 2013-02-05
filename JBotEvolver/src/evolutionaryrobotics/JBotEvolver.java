package evolutionaryrobotics;

import java.io.IOException;
import java.util.ArrayList;

import result.Result;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;
import taskexecutor.TaskExecutor;
import tasks.Task;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import factories.EvaluationFunctionFactory;
import factories.EvolutionFactory;
import factories.PopulationFactory;

public class JBotEvolver extends JBotSim {
	
	private DiskStorage diskStorage;
	private TaskExecutor taskExecutor;
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
		taskExecutor = TaskExecutor.getTaskExecutor(arguments.get("--executor"));
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
	
	public Evolution getEvolution() {
		return Evolution.getEvolution(this,arguments.get("--evolution"));
	}
	
	public void setChromosome(ArrayList<Robot> robots, Chromosome chromosome) {
		for (Robot r : robots) {
			if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
				controller.setNNWeights(chromosome.getAlleles());
			}
		}	
	}
	
	public void submitTask(Task task) {
		
	}

	public Result getResult() {
		return null;
	}
	
	@Override
	protected void loadArguments(String[] args) throws IOException, ClassNotFoundException {
		super.loadArguments(args);
		diskStorage = new DiskStorage(arguments.get("--output").getCompleteArgumentString());
	}

	public DiskStorage getDiskStorage() {
		return diskStorage;
	}
}