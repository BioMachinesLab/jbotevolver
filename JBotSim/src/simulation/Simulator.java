package simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import comm.FileProvider;

import experiments.Experiment;

import mathutils.Vector2d;
import simulation.environment.Environment;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.SimRandom;

public class Simulator implements Serializable {

	public static final int MAXNUMBEROFROBOTS = 100000;

	protected Double time = Double.valueOf(0);
	protected double timeDelta = 0.1;
	protected Environment environment;
	protected SimRandom random;
	protected Experiment experiment;
	protected FileProvider fileProvider = FileProvider.getDefaultFileProvider();
	private int numberRobots = 0;
	private int numberPhysicalObjects = 0;
	private ArrayList<Runnable> callbacks = new ArrayList<Runnable>(); 

	private GeometricCalculator calculator;
	
	public Simulator(SimRandom random) {
		this.random = random;
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

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public Experiment getExperiment() {
		return experiment;
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

	public void start() {

	}
	
	public void addCallback(Runnable r) {
		callbacks.add(r);
	}
	
	public void removeCallback(Runnable r) {
		callbacks.remove(r);
	}

	public void stop() {
		if(experiment != null)
			experiment.endExperiment();
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

	protected void updateAllRobotSensors(Double time) {
		ArrayList<PhysicalObject> teleported = environment.getTeleported();
		for (Robot r : environment.getRobots()) {
			r.updateSensors(time, teleported);
		}
		environment.clearTeleported();
	}

	protected void updateAllRobotActuators(Double time) {
		ArrayList<Robot> robots = (ArrayList<Robot>) environment.getRobots()
				.clone();
		Collections.shuffle(robots,random);
		for (Robot r : robots) {
			r.updateActuators(time, timeDelta);
		}
	}

	protected void updatePositions(Double time) {
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

	// public Environment getEnvironment() {
	// return this.environment;
	// }

	/*public double getDistanceBetween(Vector2d fromPoint, PhysicalObject toObject) {
		return environment.getDistanceBetween(fromPoint, toObject, time);
	}*/

	public int getAndIncrementNumberRobots() {
		return numberRobots++;
	}

	public void addNumberOfRobots() {
		this.numberRobots++;
	}

	public int getAndIncrementNumberPhysicalObjects() {
		return MAXNUMBEROFROBOTS + numberPhysicalObjects++;
	}

	public void addNumbetrOfPhysicalObjects() {
		this.numberPhysicalObjects++;
	}

	public void pause() {
		if(experiment != null)
			for(Robot r : experiment.getRobots())
				r.getController().pause();
	}
}
