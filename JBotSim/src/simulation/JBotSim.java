package simulation;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import factories.ControllerFactory;
import factories.EnvironmentFactory;
import factories.RobotFactory;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class JBotSim {
	
	protected Arguments experimentArguments  = null;
	protected Arguments environmentArguments = null;
	protected Arguments robotArguments       = null;
	protected Arguments controllerArguments  = null;
	protected Arguments guiArguments         = null;
	
	protected SimRandom simRandom  = new SimRandom();
	protected long      randomSeed = 0;
	
	protected String[]  commandlineArguments = null;
	protected Simulator simulator;
	
	public JBotSim(String[] commandLineArgs) throws IOException, ClassNotFoundException {
		if (commandLineArgs != null) {
			parseArguments(commandLineArgs);
		}	
	}
	
	public void parseArguments(String[] args) throws IOException, ClassNotFoundException{		
		HashMap<String,Arguments> arguments = Arguments.parseArgs(args);
		
		guiArguments          = arguments.get("--gui");
		environmentArguments  = arguments.get("--environment");
		robotArguments        = arguments.get("--robots");
		controllerArguments   = arguments.get("--controllers");
		System.out.println(robotArguments);
		System.out.println(controllerArguments);
		
		
		
		if(arguments.get("--random-seed") != null)
			randomSeed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());			
		
		simRandom.setSeed(randomSeed);
			
		//split on whitespace
		commandlineArguments = arguments.get("commandline").getCompleteArgumentString().split("\\s+");
	}

	
	public Simulator createSimulator() {	
		SimRandom simRandom = new SimRandom();
		simulator = new Simulator(simRandom);

		Environment environment = simulator.getEnvironmentFactory().getEnvironment(environmentArguments);
		simulator.setEnvironment(environment);
		environment.setup();
		
		return simulator;
	}

	public LinkedList<Robot> createRobots(int numberOfRobots) {
		LinkedList<Robot> result = new LinkedList<Robot>();
		
		for (int i = 0; i < numberOfRobots; i++) {
			Robot r = createOneRobot(robotArguments, controllerArguments);
			result.add(r);
//			simulator.getEnvironment().addRobot(r);
		}
		
		return result;
	}
	
	protected Robot createOneRobot(Arguments robotArguments, Arguments controllerArguments) {
		Robot robot = simulator.getRobotFactory().getRobot(robotArguments);
		robot.setController(simulator.getControllerFactory().getController(robot, controllerArguments));
		robot.setPosition((simRandom.nextDouble() - 0.5) * simulator.getEnvironment().getWidth(), (simRandom.nextDouble() - 0.5) * simulator.getEnvironment().getHeight());
		return robot;
	}
}
