package evolutionaryrobotics;

import java.io.IOException;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evolution.Evolution;
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
		populationFactory = new PopulationFactory(simulator);
		Population population = populationFactory.getPopulation(populationArguments);
		
		evolutionFactory = new EvolutionFactory(simulator,population);
		evolutionArguments = jBotSim.getArguments().get("--evolution");
		
		Evolution evolution = evolutionFactory.getEvolution(evolutionArguments);
		evolution.executeEvolution();
	}
	
	public static void main(String[] args) throws Exception {
		new JBotEvolver(args);
	}
}