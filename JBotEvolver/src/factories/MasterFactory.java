//package factories;
//
//import java.io.Serializable;
//
//import simulation.Simulator;
//import simulation.util.Arguments;
//
//import comm.ClientPriority;
//
//import evolutionaryrobotics.parallel.Client;
//import evolutionaryrobotics.parallel.Master;
//import evolutionaryrobotics.parallel.MultiMaster;
//import evolutionaryrobotics.parallel.ParallellerClient;
//import evolutionaryrobotics.parallel.ParallellerCoevolutionClient;
//import evolutionaryrobotics.populations.Population;
//import evolutionaryrobotics.util.DiskStorage;
//import experiments.Experiment;
//
//public class MasterFactory extends Factory implements Serializable {
//
//	private static final int DEFAULT_NUMBER_OF_OPPONENTS = 10;
//
//	public static Master getMaster(Simulator simulator, Arguments arguments, Arguments experimentArguments,
//			Arguments environmentArguments, Arguments robotArguments,
//			Arguments controllerArguments, Population population,
//			Arguments evaluationArguments, DiskStorage diskStorage) {
//		if (arguments == null) {
//			return new Master(simulator, null, experimentArguments,
//					environmentArguments, robotArguments, controllerArguments,
//					population, evaluationArguments, diskStorage, true);
//		}
//
//		boolean withGui = true;
//		if (arguments.getArgumentIsDefined("withoutgui")) {
//			withGui = false;
//		}
//
//		if (arguments.getArgumentIsDefined("withgui")) {
//			withGui = true;
//		}
//
//		String slaveFile = null;
//		if (arguments.getArgumentIsDefined("slavefile")) {
//			slaveFile = arguments.getArgumentAsString("slavefile");
//		}
//
//		Master master = new Master(simulator, slaveFile, experimentArguments,
//				environmentArguments, robotArguments, controllerArguments,
//				population, evaluationArguments, diskStorage, withGui);
//		return master;
//	}
//
//	public static Client getClient(Arguments arguments, Arguments experimentArguments,
//			Arguments environmentArguments, Arguments robotArguments,
//			Arguments controllerArguments, Population population,
//			Arguments evaluationArguments, DiskStorage diskStorage) {
//		if (arguments == null) {
//			return new Client(simulator, MultiMaster.LOW_PRIORITY, null,
//					MultiMaster.DEFAULTCLIENTMASTERPORT, experimentArguments,
//					environmentArguments, robotArguments, controllerArguments,
//					population, evaluationArguments, diskStorage);
//		}
//		int priority = MultiMaster.LOW_PRIORITY;
//		if (arguments.getArgumentIsDefined("priority")) {
//			priority = arguments.getArgumentAsInt("priority");
//			;
//		}
//		int port = MultiMaster.DEFAULTCLIENTMASTERPORT;
//		if (arguments.getArgumentIsDefined("port")) {
//			priority = arguments.getArgumentAsInt("port");
//			;
//		}
//
//		String serverName = "evolve.dcti.iscte.pt";
//		if (arguments.getArgumentIsDefined("server")) {
//			serverName = arguments.getArgumentAsString("server");
//		}
//
//		return new Client(simulator, priority, serverName, port,
//				experimentArguments, environmentArguments, robotArguments,
//				controllerArguments, population, evaluationArguments,
//				diskStorage);
//
//	}
//
//	public ParallellerClient getPPClient(Arguments arguments,
//			Arguments experimentArguments, Arguments environmentArguments,
//			Arguments robotArguments, Arguments controllerArguments,
//			Population population, Arguments evaluationArguments,
//			DiskStorage diskStorage) {
//		int serverPort = 0;
//		int codePort = 0;
//		if (arguments == null) {
//			return new ParallellerClient(simulator, ClientPriority.LOW,
//					"localhost", serverPort, "localhost", codePort,
//					experimentArguments, environmentArguments, robotArguments,
//					controllerArguments, population, evaluationArguments,
//					diskStorage);
//		}
//		ClientPriority priority = ClientPriority.LOW;
//		if (arguments.getArgumentIsDefined("priority")) {
//			int pn = arguments.getArgumentAsInt("priority");
//			priority = (pn < 2) ? ClientPriority.VERY_HIGH
//					: (pn < 4) ? ClientPriority.HIGH
//							: (pn < 7) ? ClientPriority.NORMAL
//									: ClientPriority.LOW;
//		}
//		if (arguments.getArgumentIsDefined("serverport")) {
//			serverPort = arguments.getArgumentAsInt("serverport");
//		}
//		if (arguments.getArgumentIsDefined("codeport")) {
//			codePort = arguments.getArgumentAsInt("codeport");
//		}
//
//		String serverName = "evolve.dcti.iscte.pt";
//		if (arguments.getArgumentIsDefined("server")) {
//			serverName = arguments.getArgumentAsString("server");
//		}
//
//		return new ParallellerClient(simulator, priority, serverName,
//				serverPort, serverName, codePort, experimentArguments,
//				environmentArguments, robotArguments, controllerArguments,
//				population, evaluationArguments, diskStorage);
//	}
//
//	public ParallellerCoevolutionClient getCoevolutionPPClient(
//			Arguments arguments, Arguments experimentArguments,
//			Arguments environmentArguments, Arguments robotArguments,
//			Arguments controllerArguments, Arguments populationArguments,
//			Arguments evaluationArguments, Experiment experiment,
//			DiskStorage diskStorage) {
//		int serverPort = 0;
//		int codePort = 0;
//		if (arguments == null) {
//			return new ParallellerCoevolutionClient(simulator,
//					ClientPriority.LOW, "localhost", serverPort, "localhost",
//					codePort, experimentArguments, environmentArguments,
//					robotArguments, controllerArguments, populationArguments,
//					evaluationArguments, experiment, diskStorage, DEFAULT_NUMBER_OF_OPPONENTS);
//		}
//		int numberOfOpponents = arguments
//				.getArgumentIsDefined("numberOfOpponents") ? arguments
//				.getArgumentAsInt("numberOfOpponents") : DEFAULT_NUMBER_OF_OPPONENTS;
//
//		ClientPriority priority = ClientPriority.LOW;
//		if (arguments.getArgumentIsDefined("priority")) {
//			int pn = arguments.getArgumentAsInt("priority");
//			priority = (pn < 2) ? ClientPriority.VERY_HIGH
//					: (pn < 4) ? ClientPriority.HIGH
//							: (pn < 7) ? ClientPriority.NORMAL
//									: ClientPriority.LOW;
//		}
//		if (arguments.getArgumentIsDefined("serverport")) {
//			serverPort = arguments.getArgumentAsInt("serverport");
//		}
//		if (arguments.getArgumentIsDefined("codeport")) {
//			codePort = arguments.getArgumentAsInt("codeport");
//		}
//
//		String serverName = "evolve.dcti.iscte.pt";
//		if (arguments.getArgumentIsDefined("server")) {
//			serverName = arguments.getArgumentAsString("server");
//		}
//
//		return new ParallellerCoevolutionClient(simulator, priority,
//				serverName, serverPort, serverName, codePort,
//				experimentArguments, environmentArguments, robotArguments,
//				controllerArguments, populationArguments, evaluationArguments,
//				experiment, diskStorage, numberOfOpponents);
//	}
//
//}
