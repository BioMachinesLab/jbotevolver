package simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import simulation.environment.Environment;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;
import comm.FileProvider;

public class Simulator implements Serializable {

	private static int maxNumberRobots = 100000;
	protected Double time = Double.valueOf(0);
	protected double timeDelta = 0.1;
	protected Environment environment;
	protected Random random;
	protected FileProvider fileProvider = FileProvider.getDefaultFileProvider();
	private int numberRobots = 0;
	private int numberPhysicalObjects = 0;
	private ArrayList<Updatable> callbacks = new ArrayList<Updatable>(); 
	private GeometricCalculator calculator;
	private boolean stopSimulation = false;
	private int[] robotIndexes;
	private boolean setup = false;
	
	private HashMap<String,Arguments> arguments = new HashMap<String,Arguments>(); 
	
	public Simulator(Random random, HashMap<String,Arguments> arguments) {
		this.random = random;
		this.arguments = arguments;
		this.environment = Environment.getEnvironment(this, arguments.get("--environment"));
		calculator = new GeometricCalculator();
	}
	
	public Double getTime(){
		return time;
	}
	
	public GeometricCalculator getGeoCalculator(){
		return this.calculator;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Random getRandom() {
		return random;
	}

	public FileProvider getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	public void addCallback(Updatable r) {
		callbacks.add(r);
	}
	
	public void removeCallback(Updatable r) {
		callbacks.remove(r);
	}

	public void performOneSimulationStep(Double time) {
		this.time = time;
		
		// Update the readings for all the sensors:
		updateAllRobotSensors(time);
		// Call the controllers:
		updateAllControllers(time);
		// Compute the actions of the robot's actuators on the environment and on itself
		updateAllRobotActuators(time);
		// Update non-robot objects in the environment
		updateEnvironment(time);
		// Update the positions of everything
		updatePositions(time);
		
		for (Updatable r : callbacks) {
			r.update(this);
		}
	}

	protected void updateAllControllers(Double time) {
		for (Robot r : environment.getRobots()) {
			r.getController().controlStep(time);
		}
	}

	protected void updateEnvironment(Double time) {
		environment.update(time);
	}

	protected void updateAllRobotSensors(double time) {
		ArrayList<PhysicalObject> teleported = environment.getTeleported();
		for (Robot r : environment.getRobots()) {
			r.updateSensors(time, teleported);
		}
		environment.clearTeleported();
	}

	protected void updateAllRobotActuators(double time) {
		ArrayList<Robot> robots = (ArrayList<Robot>) environment.getRobots();
		
		if(robotIndexes == null || robotIndexes.length != robots.size())
			createRobotIndexes(robots.size());
		
		Collections.shuffle(Arrays.asList(robotIndexes),random);
		
		for (int i = 0 ; i < robotIndexes.length ; i++)
			robots.get(robotIndexes[i]).updateActuators(time, timeDelta);
	}
	
	private void createRobotIndexes(int size) {
		robotIndexes = new int[size];
		for(int i = 0 ; i < size ; i++)
			robotIndexes[i] = i;
	}

	protected void updatePositions(double time) {
		environment.updateCollisions(time);
	}

	public void simulate() {
		setup();
		for (time = Double.valueOf(0); time < environment.getSteps() && !stopSimulation; time++) {
			performOneSimulationStep(time);
		}
		stopSimulation = true;
	}

	public double getTimeDelta() {
		return timeDelta;
	}

	public int getAndIncrementNumberPhysicalObjects(PhysicalObjectType type) {
		return type == PhysicalObjectType.ROBOT ? this.numberRobots++ : maxNumberRobots + numberPhysicalObjects++;
	}
	
	public void stopSimulation() {
		stopSimulation = true;
	}
	
	public HashMap<String, Arguments> getArguments() {
		return arguments;
	}

	public boolean simulationFinished() {
		return stopSimulation;
	}

	public void addRobots(ArrayList<Robot> robots) {
		environment.addRobots(robots);
	}
	
	public ArrayList<Robot> getRobots() {
		return environment.getRobots();
	}

	private void setup() {
		if(!this.setup) {
			if(!environment.isSetup()) {
				setupEnvironment();
				if(!environment.isSetup())
					throw new RuntimeException("Overridden function 'setup' in Environment must call super.setup()");
			}
			this.setup = true;
		}
	}

	public void setupEnvironment() {
		environment.setup(this);
	}
	
	public ArrayList<Updatable> getCallbacks() {
		return callbacks;
	}
}