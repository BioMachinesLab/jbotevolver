package simulation;

import gui.Gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import comm.FileProvider;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.Factory;

public class Simulator implements Serializable {

	private static final long serialVersionUID = -2453767907508994194L;
	public static int maxNumberRobots = 100000;
	protected Double time = Double.valueOf(0);
	
	protected double timeDelta = 0.025; //0.1 timeDelta=0.025;
	
	protected Environment environment;
	protected Random random;
	protected long randomSeed;
	protected FileProvider fileProvider = FileProvider.getDefaultFileProvider();
	private int numberRobots = 0;
	private int numberPhysicalObjects = 0;
	private ArrayList<Updatable> callbacks = new ArrayList<Updatable>();
	private boolean stopSimulation = false;
	private int[] robotIndexes;
	private boolean setup = false;

	private boolean parallel = false;
	private ArrayList<StagedParallelRobotCallable> runnables;

	private Network network;

	private HashMap<String, Arguments> arguments = new HashMap<String, Arguments>();
	protected HashMap<String,Serializable> serializableObjects;

	public Simulator(long randomSeed, HashMap<String, Arguments> arguments) {
		
		this.random = new Random(randomSeed);
		this.randomSeed = randomSeed;
		this.arguments = arguments;
		this.environment = Environment.getEnvironment(this, arguments.get("--environment"));

		Arguments args = arguments.get("--simulator");

		if (args != null) {
			timeDelta = args.getArgumentAsDoubleOrSetDefault("timedelta", timeDelta);
			parallel = args.getArgumentAsIntOrSetDefault("parallel", 0) == 1;

			if (args.getArgumentIsDefined("fixedseed") && args.getArgumentAsDouble("fixedseed") != 0) {
				this.randomSeed = args.getArgumentAsInt("fixedseed");
				this.random = new Random(args.getArgumentAsInt("fixedseed"));
			}

			if (args.getArgumentIsDefined("network")) {
				network = Network.getNetwork(this, new Arguments(args.getArgumentAsString("network"), true));
			}

			parallel = args.getArgumentAsIntOrSetDefault("parallel", 0) == 1;
		}

		args = arguments.get("--updatables");

		if (args != null) {
			for (int i = 0; i < args.getNumberOfArguments(); i++) {
				Arguments updatableArgs = new Arguments(args.getArgumentAsString(args.getArgumentAt(i)));
				addCallback(
						(Updatable) Factory.getInstance(updatableArgs.getArgumentAsString("classname"), updatableArgs));
			}
		}
		
	}
	
	public void setSerializableObjects(HashMap<String, Serializable> serializableObjects) {
		this.serializableObjects = serializableObjects;
	}
	
	public HashMap<String, Serializable> getSerializableObjects() {
		return serializableObjects;
	}

	public Network getNetwork() {
		return network;
	}

	public Double getTime() {
		return time;
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

		if (parallel) {
			if (runnables == null) {
				runnables = new ArrayList<StagedParallelRobotCallable>();
				for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
					runnables.add(new StagedParallelRobotCallable(i));
			}
			// for(int i = 0 ; i < 3 ; i++) {
			// for(Callable<Object> c : runnables)
			// ((StagedParallelRobotCallable)c).stage=i;
			// try {
			// pool.invokeAll(runnables);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }
			// environment.clearTeleported();

			try {
				Thread[] t = new Thread[Runtime.getRuntime().availableProcessors()];
				for (int i = 0; i < t.length; i++) {
					t[i] = new Thread(runnables.get(i));
					t[i].start();
				}
				for (int i = 0; i < t.length; i++) {
					t[i].join();
				}
				environment.clearTeleported();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			// Update the readings for all the sensors:
			// long d = System.currentTimeMillis();
			updateAllRobotSensors(time);
			
			// Call the controllers:
			updateAllControllers(time);
			
			
			
			
			// Compute the actions of the robot's actuators on the environment
			// and on itself
			updateAllRobotActuators(time);
			// d = System.currentTimeMillis()-d;
			// d/=environment.getRobots().size();
			// System.out.println(d);
		}
		// Update non-robot objects in the environment
		updateEnvironment(time);
		// Update the positions of everything
		updatePositions(time);

		for (Updatable r : callbacks) {
//			if(r instanceof Gui)
//				continue;
			r.update(this);
		}
	}

	protected void updateAllControllers(Double time) {
		for (Robot r : environment.getRobots()) {
			if (r.isEnabled())
				if (r.getController() != null)
					r.getController().controlStep(time);
		}
	}

	protected void updateEnvironment(Double time) {
		environment.update(time);
	}

	protected void updateAllRobotSensors(double time) {
		ArrayList<PhysicalObject> teleported = environment.getTeleported();
		for (Robot r : environment.getRobots()) {
			if (r.isEnabled())
				r.updateSensors(time, teleported);
		}
		environment.clearTeleported();
	}

	protected void updateAllRobotActuators(double time) {
		ArrayList<Robot> robots = environment.getRobots();

		if (robotIndexes == null || robotIndexes.length != robots.size())
			createRobotIndexes(robots.size());

		Collections.shuffle(Arrays.asList(robotIndexes), random);

		for (int i = 0; i < robotIndexes.length; i++)
			if (robots.get(robotIndexes[i]).isEnabled())
				robots.get(robotIndexes[i]).updateActuators(time, timeDelta);
	}

	private void createRobotIndexes(int size) {
		robotIndexes = new int[size];
		for (int i = 0; i < size; i++)
			robotIndexes[i] = i;
	}

	public void updatePositions(double time) {
		environment.updateCollisions(time);
	}

	public void simulate() {
		setup();
		for (time = Double.valueOf(0); time < environment.getSteps() && !stopSimulation; time++) {
			performOneSimulationStep(time);
		}
		terminate();
	}

	public void simulate(long sleepTime) {
		
		setup();
		for (time = Double.valueOf(0); time < environment.getSteps() && !stopSimulation; time++) {
			performOneSimulationStep(time);

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
		}
		terminate();
	}

	public void terminate() {
		stopSimulation = true;
		if (network != null)
			network.shutdown();
		for (Updatable u : callbacks) {
			if (u instanceof Stoppable) {
				Stoppable s = (Stoppable) u;
				s.terminate(this);
			}
		}
		for (Robot r : environment.getRobots()) {
			r.getController().end();
		}
	}

	public double getTimeDelta() {
		return timeDelta;
	}

	public long getRandomSeed() {
		return randomSeed;
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
		if (!this.setup) {
			if (!environment.isSetup()) {
				setupEnvironment();
				if (!environment.isSetup())
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

	class ParallelRobotCallable implements Callable<Object> {

		private Robot r;

		public ParallelRobotCallable(Robot r) {
			this.r = r;
		}

		@Override
		public Object call() {
			if (r.isEnabled()) {
				// long d = System.currentTimeMillis();
				r.updateSensors(time, environment.getTeleported());
				r.getController().controlStep(time);
				r.updateActuators(time, timeDelta);
				// d=System.currentTimeMillis()-d;
				// System.out.println(d);
			}
			return r;
		}
	}

	class StagedParallelRobotCallable implements Runnable {

		// private ArrayList<Robot> robots = new ArrayList<Robot>();
		public int stage = 0;
		public int start = 0;
		public int end = 0;

		public StagedParallelRobotCallable(int i) {
			int total = environment.getRobots().size() / Runtime.getRuntime().availableProcessors();
			start = total * i;
			end = total * (i + 1);

			if (i == Runtime.getRuntime().availableProcessors() - 1)
				end = environment.getRobots().size();

		}

		@Override
		public void run() {
			for (int j = start; j < end; j++) {
				Robot r = environment.getRobots().get(j);
				if (r.isEnabled()) {
					r.updateSensors(time, environment.getTeleported());
					r.getController().controlStep(time);
					r.updateActuators(time, timeDelta);
				}
			}
		}
	}
}