package simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import comm.FileProvider;
import factories.ControllerFactory;
import factories.EnvironmentFactory;
import factories.RobotFactory;
import simulation.environment.Environment;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.SimRandom;

public class Simulator implements Serializable {

	public final int MAXNUMBEROFROBOTS = 100000;

	protected Double time = Double.valueOf(0);
	protected double timeDelta = 0.1;
	protected Environment environment;
	protected SimRandom random;
	protected FileProvider fileProvider = FileProvider.getDefaultFileProvider();
	private int numberRobots = 0;
	private int numberPhysicalObjects = 0;
	private ArrayList<Runnable> callbacks = new ArrayList<Runnable>(); 
	
	protected ControllerFactory      controllerFactory; 
	protected RobotFactory           robotFactory;
	protected EnvironmentFactory     environmentFactory;

	private GeometricCalculator calculator;
	
	public Simulator(SimRandom random) {
		this.random = random;
		environmentFactory = new EnvironmentFactory(this);
		controllerFactory  = new ControllerFactory(this);
		robotFactory       = new RobotFactory(this);
		calculator = new GeometricCalculator();
	}
	
	public Double getTime(){
		return time;
	}
	
	public GeometricCalculator getGeoCalculator(){
		return this.calculator;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public SimRandom getRandom() {
		return random;
	}

	public FileProvider getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	public void addCallback(Runnable r) {
		callbacks.add(r);
	}
	
	public void removeCallback(Runnable r) {
		callbacks.remove(r);
	}

	public void performOneSimulationStep(Double time) {
		this.time = time;
		
		// Update the readings for all the sensors:
		updateAllRobotSensors(time);
		// Call the controllers:
		updateAllControllers(time);
		// Compute the actions of the robot's actuators on the environment and
		// on itself
		updateAllRobotActuators(time);
		// Update non-robot objects in the environment
		updateEnvironment(time);
		// Update the positions of everything
		updatePositions(time);
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
		ArrayList<Robot> robots = (ArrayList<Robot>) environment.getRobots().clone();
		Collections.shuffle(robots,random);
		for (Robot r : robots) {
			r.updateActuators(time, timeDelta);
		}
	}

	protected void updatePositions(double time) {
		environment.updateCollisions(time);

		// checkAgentAgentCollisions();
		// checkAgents();
	}

	public void simulate(int numIterations) {
		for (time = Double.valueOf(0); time < numIterations; time++) {
			performOneSimulationStep(time);
			for (Runnable r : callbacks) {
				r.run();
			}
		}
	}

	public double getTimeDelta() {
		return timeDelta;
	}

	public int getAndIncrementNumberPhysicalObjects(PhysicalObjectType type) {
		return type == PhysicalObjectType.ROBOT ? this.numberRobots++ : MAXNUMBEROFROBOTS + numberPhysicalObjects++;
	}

	public void addNumbetrOfPhysicalObjects() {
		this.numberPhysicalObjects++;
	}
	
	public EnvironmentFactory getEnvironmentFactory() {
		return environmentFactory;
	}
	
	public ControllerFactory getControllerFactory() {
		return controllerFactory;
	}
	
	public RobotFactory getRobotFactory() {
		return robotFactory;
	}
}
