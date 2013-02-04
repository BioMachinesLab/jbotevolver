package simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;
import factories.ControllerFactory;
import factories.EnvironmentFactory;
import factories.RobotFactory;

public class JBotSim {
	
	protected HashMap<String,Arguments> arguments;
	protected Random random = new Random();
	protected String[]  commandlineArguments = null;
	
	public JBotSim(String[] commandLineArgs) throws IOException, ClassNotFoundException {
		if (commandLineArgs != null) {
			parseArguments(commandLineArgs);
		}
	}
	
	private void parseArguments(String[] args) throws IOException, ClassNotFoundException{		
		arguments = Arguments.parseArgs(args);
		
		long randomSeed = 0;
		if(arguments.get("--random-seed") != null)
			randomSeed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());			
		random.setSeed(randomSeed);
			
		//split on whitespace
		commandlineArguments = arguments.get("commandline").getCompleteArgumentString().split("\\s+");
	}
	
	public Environment getEnvironment(Simulator simulator) {
		Environment environment = EnvironmentFactory.getEnvironment(simulator, arguments.get("--environment"));
		simulator.setEnvironment(environment);
		environment.setup(simulator);
		
		return environment;
	}
	
	public Simulator createSimulator() {	
		Random simRandom = new Random();
		simRandom.setSeed(random.nextLong());
		
		Simulator simulator = new Simulator(simRandom,arguments);
		
		return simulator;
	}
	
	public ArrayList<Robot> createRobots(Simulator simulator, int numberOfRobots) {
		ArrayList<Robot> result = new ArrayList<Robot>(numberOfRobots);
		
		for (int i = 0; i < numberOfRobots; i++) {
			Robot r = createOneRobot(simulator, arguments.get("--robots"), arguments.get("--controllers"));
			result.add(r);
		}
		return result;
	}
	
	public ArrayList<Robot> createRobots(Simulator simulator) {
		return RobotFactory.getRobots(simulator, arguments.get("--robots"));
	}
	
	protected Robot createOneRobot(Simulator simulator, Arguments robotArguments, Arguments controllerArguments) {
		Robot robot = RobotFactory.getRobot(simulator, robotArguments);
		robot.setController(ControllerFactory.getController(simulator,robot, controllerArguments));
		return robot;
	}
	
	public HashMap<String, Arguments> getArguments() {
		return arguments;
	}
	
	public Random getRandom() {
		return random;
	}
}