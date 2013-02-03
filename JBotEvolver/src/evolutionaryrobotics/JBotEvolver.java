package evolutionaryrobotics;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import evolutionaryrobotics.populations.Population;
import factories.EvolutionFactory;
import factories.PopulationFactory;

public class JBotEvolver {
	
	private JBotSim jBotSim;
	private EvolutionFactory evolutionFactory;
	private PopulationFactory populationFactory;
	private Simulator simulator;
	private Arguments evolutionArguments;
	private Arguments populationArguments;
	
	public JBotEvolver(String[] args) throws Exception {
		jBotSim = new JBotSim(args);
		simulator = jBotSim.createSimulator();
		
		populationArguments = jBotSim.getArguments().get("--population");
		populationArguments.setArgument("genomelength", getGenomeLength());
		populationFactory = new PopulationFactory(simulator);
		Population population = populationFactory.getPopulation(populationArguments);
		
		evolutionFactory = new EvolutionFactory(simulator,population);
		evolutionArguments = jBotSim.getArguments().get("--evolution");
		Evolution evolution = evolutionFactory.getEvolution(evolutionArguments);
		
		evolution.executeEvolution();
	}
	
	private int getGenomeLength() {
		
		Robot r = simulator.getRobotFactory().getRobot(jBotSim.getArguments().get("--robots"));
		Controller c = simulator.getControllerFactory().getController(r, jBotSim.getArguments().get("--controllers"));
		
		int genomeLength = 0;
		
		if(c instanceof NeuralNetworkController) {
			NeuralNetworkController nnc = (NeuralNetworkController)c;
			genomeLength = nnc.getNeuralNetwork().getGenomeLength();
		}
		return genomeLength;
	}
	
	public static void main(String[] args) throws Exception {
		new JBotEvolver(args);
	}
}