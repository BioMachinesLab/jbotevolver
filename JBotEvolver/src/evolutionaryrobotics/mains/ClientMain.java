package evolutionaryrobotics.mains;

import simulation.Simulator;
import simulation.util.SimRandom;
import evolutionaryrobotics.Main;
import evolutionaryrobotics.parallel.ParallellerClient;
import evolutionaryrobotics.parallel.ParallellerCoevolutionClient;
import evolutionaryrobotics.populations.Population;
import experiments.Experiment;
import factories.ExperimentFactory;
import factories.MasterFactory;
import factories.PopulationFactory;
import gui.ClientGui;

public class ClientMain extends Main{
	
	public synchronized ParallellerClient getNewClient() throws Exception{
		
		if (actAsCoevolutionClient) {
			diskStorage.start();
			diskStorage.saveCommandlineArguments(commandlineArguments);
			experiment = new ExperimentFactory(simulator)
					.getCoevolutionExperiment(experimentArguments,
							environmentArguments, robotsArguments,
							controllersArguments);
			ParallellerCoevolutionClient client = (new MasterFactory(
					simulator)).getCoevolutionPPClient(masterArguments,
					experimentArguments, environmentArguments,
					robotsArguments, controllersArguments,
					populationArguments, evaluationArguments,
					experiment, diskStorage);
			return client;
		}else{
			SimRandom simRandom = new SimRandom();
			simRandom.setSeed(randomSeed);
			
			Simulator sim = new Simulator(simRandom);
			
			Experiment experiment = (new ExperimentFactory(sim)).getExperiment(
					experimentArguments, environmentArguments, robotsArguments,
					controllersArguments);
			sim.setEnvironment(experiment.getEnvironment());
			
			Population population = new PopulationFactory(sim)
					.getPopulation(populationArguments, experiment.getGenomeLength());
			
			ParallellerClient client = (new MasterFactory(sim))
			.getPPClient(masterArguments,
					experimentArguments,
					environmentArguments, robotsArguments,
					controllersArguments, population,
					evaluationArguments, diskStorage);
			diskStorage.start();
			diskStorage.saveCommandlineArguments(commandlineArguments);
			return client;
		}
	}
	
	public String getCurrentOutputPath() {
		return diskStorage.getOutputDirectory();
	}
	
	@Override
	public void execute() throws Exception {	
		new ClientGui(this);
	}
	
	public static void main(String[] args) {
		try {
			new ClientMain().execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
