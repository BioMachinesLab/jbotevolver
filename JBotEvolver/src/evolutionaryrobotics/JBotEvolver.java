package evolutionaryrobotics;

import java.io.File;
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

public class JBotEvolver extends JBotSim {
	
	private DiskStorage diskStorage;
	private TaskExecutor taskExecutor;
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
		taskExecutor = TaskExecutor.getTaskExecutor(this,arguments.get("--executor"));
		taskExecutor.setDaemon(true);
		taskExecutor.start();
	}
	
	public EvaluationFunction getEvaluationFunction(Simulator simulator) {
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
	
	@Override
	protected void loadArguments(String[] args) throws IOException, ClassNotFoundException {
		super.loadArguments(args);
		
		String absolutePath = "";
		
		if(arguments.get("--output") != null)
			absolutePath = (new File("./"+arguments.get("--output").getCompleteArgumentString())).getCanonicalPath();
		
		if(parentFolder.isEmpty())
			arguments.get("--population").setArgument("parentfolder", absolutePath);
		else
			arguments.get("--population").setArgument("parentfolder", parentFolder);
		
		diskStorage = new DiskStorage(arguments.get("--output").getCompleteArgumentString());
		diskStorage.start();
	}

	public DiskStorage getDiskStorage() {
		return diskStorage;
	}
	
	public static void main(String[] args) throws Exception {
		JBotEvolver j = new JBotEvolver(new String[]{"left_primitive.conf"});
		j.getEvolution().executeEvolution();
	}
}