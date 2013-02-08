package simulation;

import gui.Gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import controllers.Controller;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class JBotSim {
	
	protected HashMap<String,Arguments> arguments;
	protected Random random = new Random();
	protected String parentFolder = "";
	
	public JBotSim(String[] commandLineArgs) throws IOException, ClassNotFoundException {
		if (commandLineArgs != null)
			loadArguments(commandLineArgs);
	}
	
	public Simulator createSimulator(Random random) {	
		Simulator simulator = new Simulator(random,arguments);
		return simulator;
	}
	
	public Simulator createSimulator() {
		long seed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());
		return createSimulator(new Random(seed));
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
		ArrayList<Robot> robots = Robot.getRobots(simulator, arguments.get("--robots"));
		for(Robot r : robots) {
			r.setController(Controller.getController(simulator, r, arguments.get("--controllers")));
		}
		return robots;
	}
	
	protected Robot createOneRobot(Simulator simulator, Arguments robotArguments, Arguments controllerArguments) {
		Robot robot = Robot.getRobot(simulator, robotArguments);
		robot.setController(Controller.getController(simulator,robot, controllerArguments));
		return robot;
	}
	
	public HashMap<String, Arguments> getArguments() {
		return arguments;
	}
	
	public Random getRandom() {
		return random;
	}
	
	public void savePath(String file) {
		parentFolder = (new File(file)).getParent();
	}
	
	protected void loadFile(String fileName) throws Exception {
		loadArguments(Arguments.readOptionsFromFile(fileName));
	}
	
	protected void loadArguments(String[] args) throws IOException,ClassNotFoundException {
		
		arguments = Arguments.parseArgs(args);
		
		long randomSeed = 0;
		if(arguments.get("--random-seed") != null)
			randomSeed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());			
		random.setSeed(randomSeed);
	}
	
	public void loadFile(String filename, String extraArguments) throws IOException, ClassNotFoundException {

		savePath(filename);
		
		String fileContents = Arguments.readContentFromFile(filename);
		fileContents+="\n"+extraArguments;
		
		String[] args = Arguments.readOptionsFromString(fileContents);
		
		loadArguments(args);
	}

	public Gui getGui() {
		return Gui.getGui(this,arguments.get("--gui"));
	}
}