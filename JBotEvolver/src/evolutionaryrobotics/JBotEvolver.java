package evolutionaryrobotics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import result.Result;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.ClassSearchUtils;
import taskexecutor.TaskExecutor;
import tasks.Task;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import gui.Gui;

public class JBotEvolver extends JBotSim {
	
	private DiskStorage diskStorage;
	private TaskExecutor taskExecutor;
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
		if(arguments.get("--gui") != null)
			Gui.getGui(this,arguments.get("--gui"));
	}
	
	public EvaluationFunction getEvaluationFunction() {
		return EvaluationFunction.getEvaluationFunction(arguments.get("--evaluation"));
	}
	
	public Population getPopulation() {
		try {
			return Population.getPopulation(getArguments().get("--population"));
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public Evolution getEvolution() {
		if(arguments.get("--executor") != null) {
			taskExecutor = TaskExecutor.getTaskExecutor(this,arguments.get("--executor"));
			taskExecutor.setDaemon(true);
			taskExecutor.start();
		}
		if(arguments.get("--output") != null) {
			diskStorage = new DiskStorage(arguments.get("--output").getCompleteArgumentString());
			try {
				diskStorage.start();
				diskStorage.saveCommandlineArguments(arguments);
			} catch(Exception e) {e.printStackTrace(); System.exit(-1);}
		}
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
		taskExecutor.addTask(task);
	}

	public Result getResult() {
		return taskExecutor.getResult();
	}
	
	public void evolutionFinished() {
		taskExecutor.stopTasks();
	}
	
	@Override
	protected void loadArguments(String[] args) throws IOException, ClassNotFoundException {
		super.loadArguments(args);
		
		String absolutePath = "";
		
		if(arguments.get("--output") != null)
			absolutePath = (new File("./"+arguments.get("--output").getCompleteArgumentString())).getCanonicalPath();
		
		if(arguments.get("--population") != null) {
			if(parentFolder.isEmpty())
				arguments.get("--population").setArgument("parentfolder", absolutePath);
			else
				arguments.get("--population").setArgument("parentfolder", parentFolder);
		}
	}

	public DiskStorage getDiskStorage() {
		return diskStorage;
	}
}