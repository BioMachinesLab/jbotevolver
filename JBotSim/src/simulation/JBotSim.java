package simulation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import controllers.Controller;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class JBotSim implements Serializable{
	
	protected HashMap<String,Arguments> arguments;
	protected String parentFolder = "";
	protected long randomSeed;
	
	public JBotSim(HashMap<String,Arguments> arguments, long randomSeed) {
		Locale.setDefault(new Locale("en","US"));
		this.arguments = arguments;
		this.randomSeed = randomSeed;
	}
	
	public JBotSim(String[] commandLineArgs) throws IOException, ClassNotFoundException {
		Locale.setDefault(new Locale("en","US"));
		if (commandLineArgs != null)
			loadArguments(commandLineArgs);
	}
	
	public Simulator createSimulator(Random random) {	
		Simulator simulator = new Simulator(random,arguments);
		return simulator;
	}
	
	public Simulator createSimulator() {
		
		if(arguments.get("--random-seed") == null)
			throw new RuntimeException("Random seed is null!");
		
		long seed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());
		return createSimulator(new Random(seed));
	}
	
	public ArrayList<Robot> createRobots(Simulator simulator) {
		ArrayList<Robot> robots = Robot.getRobots(simulator, arguments.get("--robots"));
		for(Robot r : robots) {
			r.setController(Controller.getController(simulator, r, arguments.get("--controllers")));
		}		
		return robots;
	}
	
	public ArrayList<Robot> createCoEvolutionRobots(Simulator simulator) {
		ArrayList<Robot> robots = new ArrayList<Robot>();
		
		ArrayList<Robot> preys = Robot.getRobots(simulator, arguments.get("--robots"));
		for(Robot r : preys) {
			r.setController(Controller.getController(simulator, r, arguments.get("--controllers")));
			robots.add(r);
		}
		ArrayList<Robot> predators = Robot.getRobots(simulator, arguments.get("--robots1"));
		for(Robot r : predators) {
			r.setController(Controller.getController(simulator, r, arguments.get("--controllers1")));
			robots.add(r);
		}
		
		return robots;
	}
	
	public void createRobotsAndAddToSimulator(Simulator simulator) {
		simulator.addRobots(createRobots(simulator));
	}
	
	protected Robot createOneRobot(Simulator simulator, Arguments robotArguments, Arguments controllerArguments) {
		Robot robot = Robot.getRobot(simulator, robotArguments);
		robot.setController(Controller.getController(simulator,robot, controllerArguments));
		return robot;
	}
	
	public HashMap<String, Arguments> getArguments() {
		return arguments;
	}
	
	public void savePath(String file) {
		parentFolder = (new File(file)).getParent();
	}
	
	protected void loadFile(String fileName) throws Exception {
		loadArguments(Arguments.readOptionsFromFile(fileName));
	}
	
	protected void loadArguments(String[] args) throws IOException,ClassNotFoundException {
		
		arguments = Arguments.parseArgs(args);
		randomSeed = 0;
		
		if(arguments.get("--random-seed") != null) {
			randomSeed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());
		}
	}
	
	public void loadFile(String filename, String extraArguments) throws IOException, ClassNotFoundException {

		savePath(filename);
		
		String fileContents = Arguments.readContentFromFile(filename);
		fileContents+="\n"+extraArguments;
		
		String[] args = Arguments.readOptionsFromString(fileContents);
		
		loadArguments(args);
	}

	public synchronized HashMap<String,Arguments> getArgumentsCopy() {
		
		HashMap<String,Arguments> newArgs = new HashMap<String, Arguments>();
		
		for(String s : arguments.keySet())
			newArgs.put(s, new Arguments(arguments.get(s).getCompleteArgumentString(),false));
		
		return newArgs;
	}
	
	public long getRandomSeed() {
		return randomSeed;
	}
}