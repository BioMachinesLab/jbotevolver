package experiments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.BehaviorController;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import factories.ControllerFactory;
import factories.EnvironmentFactory;
import factories.RobotFactory;

public class Experiment implements Serializable {
	protected Simulator simulator;
	protected Environment   environment;
	protected ArrayList<Robot> robots = new ArrayList<Robot>();
	private boolean hasEnded = false;
	public int numberOfRobots = 0;
	int robotNumber = 1;
	int numberOfStepsPerRun = 100000;

	ControllerFactory      controllerFactory; 
	protected RobotFactory robotFactory;
	EnvironmentFactory     environmentFactory;

	Arguments                    environmentArguments;
	protected Arguments          robotArguments;
	protected Arguments          controllerArguments;

	//protected Experiment  instance = null;
	
	protected double    fitness = 0;
	protected Arguments experimentArguments;

	protected double[] lastWeights;

	public Experiment(Simulator simulator, Arguments experimentArguments, Arguments environmentArguments, Arguments robotArguments, Arguments controllerArguments) {
		this.simulator = simulator;
		this.simulator.setExperiment(this);
		
		this.experimentArguments  = experimentArguments;
		this.environmentArguments = environmentArguments;
		this.robotArguments       = robotArguments;
		this.controllerArguments  = controllerArguments;

		controllerFactory    = new ControllerFactory(simulator); 
		robotFactory         = new RobotFactory(simulator);
		environmentFactory   = new EnvironmentFactory(simulator);

		if (experimentArguments != null) {
			if (experimentArguments.getArgumentIsDefined("numberofrobots")) {
				numberOfRobots = experimentArguments.getArgumentAsInt("numberofrobots");
			}
		}

		if (robotArguments != null) {
			if (robotArguments.getArgumentIsDefined("numberofrobots")) {
				numberOfRobots = robotArguments.getArgumentAsInt("numberofrobots");
			}
		}

		if(robotArguments != null){
			if(experimentArguments.getArgumentIsDefined("maxnumberofrobots")){
				numberOfRobots = simulator.getRandom().nextInt(experimentArguments.getArgumentAsInt("maxnumberofrobots"))+1;
				//System.out.println(numberOfRobots + " robots randomly created ");

			}
		}

		if(robotArguments != null){
			if(robotArguments.getArgumentIsDefined("maxnumberofrobots")){
				numberOfRobots = simulator.getRandom().nextInt(robotArguments.getArgumentAsInt("maxnumberofrobots"))+1;
				//System.out.println(numberOfRobots);

			}
		}

		//		if(numberOfRobots <2)
		//			numberOfRobots = 2;
		//System.out.println(numberOfRobots + " robots randomly created ");
		//		Environment.forgetInstance();
		createEnvironment();
	}

	
	public Environment getEnvironment() {
		return environment;
	}

	protected void createEnvironment() {
		environment = environmentFactory.getEnvironment(environmentArguments);
		simulator.setEnvironment(environment);
		//LinkedList<Robot> robots;

		robots = createRobots();

		for (Robot r : robots) {
			environment.addRobot(r);
		}
		placeRobots();
	}

	public void placeRobots() {
		// First place the robots far away from everything:
		for (Robot r : robots) {
			r.setPosition(new Vector2d(0, -10));
		}				

		if (robotArguments.getArgumentIsDefined("placement")) {
			placeRobotsUsingPlacement();
		} else {
			for (Robot r : robots) {
				r.teleportTo(new Vector2d(0, 0));
			}				
		}
		
		if(robotArguments.getArgumentIsDefined("orientation")) {
			
			double orientation = robotArguments.getArgumentAsDouble("orientation");
			
			if(robotArguments.getArgumentIsDefined("randomizeorientation"))
				orientation+=(simulator.getRandom().nextDouble()*2-1)*robotArguments.getArgumentAsDouble("randomizeorientation");
			
			for(Robot r : robots) {
				r.setOrientation(orientation);
			}
		}
	}

	protected void placeRobotsUsingPlacement() {
		if (robotArguments.getArgumentAsString("placement").equalsIgnoreCase("rectangle")) {

			//System.out.println("Placing robots in a rectangle...");
			for (Robot r : robots) {

				boolean tooCloseToSomeOtherRobot = false;

				do {
					tooCloseToSomeOtherRobot = false;		
					double positionX = environment.getWidth()  * (simulator.getRandom().nextDouble() - 0.5);
					double positionY = environment.getHeight() * (simulator.getRandom().nextDouble() - 0.5);

					r.setPosition(new Vector2d(positionX, positionY));
					Robot closestRobot = getRobotClosestTo(r);
					if (closestRobot != null) {		
						if (r.getPosition().distanceTo(closestRobot.getPosition()) < 0.3)
							tooCloseToSomeOtherRobot = true;
					}

				} while (tooCloseToSomeOtherRobot);
				r.teleportTo(r.getPosition());
			}
		}
		if(robotArguments.getArgumentAsString("placement").equalsIgnoreCase("custom"))
		{
			
			for(Robot r: robots) {
				
				do{
					
					double x = robotArguments.getArgumentAsDouble("positionx");
					double y = robotArguments.getArgumentAsDouble("positiony");
				
					if(robotArguments.getArgumentIsDefined("randomizex")) {
						double randomizeX = robotArguments.getArgumentAsDouble("randomizex");
						double distance = (simulator.getRandom().nextDouble()*2-1)*randomizeX;
						x-=distance;
					}
					if(robotArguments.getArgumentIsDefined("randomizey")) {
						double randomizeY = robotArguments.getArgumentAsDouble("randomizey");
						double distance = (simulator.getRandom().nextDouble()*2-1)*randomizeY;
						y+=distance;
					}
					
					r.teleportTo(new Vector2d(x, y));
					
					environment.updateRobotCloseObjects(0);
					environment.updateCollisions(0);
				
				} while(r.isInvolvedInCollison());				
			}
		}
	}

	public Robot getRobotClosestTo(Robot robot) {
		double closestDistance  = 1e10;
		Robot  closestRobot     = null;

		for (Robot r : robots) {
			if (r != robot) {
				if (r.getPosition().distanceTo(robot.getPosition()) < closestDistance) {
					closestDistance = r.getPosition().distanceTo(robot.getPosition());
					closestRobot    = r;	
				}
			}	
		}				
		return closestRobot;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;

	}

	public ArrayList<Robot> createRobots() {
		robots.clear();

		for (int i = 0; i < numberOfRobots; i++)
			robots.add(createOneRobot(robotArguments,controllerArguments));

		return robots;
	}
	
	public void createOneRobot() {
		Robot r = createOneRobot(this.robotArguments, this.controllerArguments);
		if(r.getEvolvingController() instanceof NeuralNetworkController){
			NeuralNetworkController nnController = (NeuralNetworkController) r.getEvolvingController();
			nnController.setNNWeights(lastWeights);
		}
		environment.addRobot(r);
		r.setPosition(new Vector2d(0, -10));
		r.teleportTo(new Vector2d(0, 0));
	}

	protected Robot createOneRobot(Arguments argumentsRobot, Arguments argumentsController) {

		Robot robot = robotFactory.getRobot(argumentsRobot);
		addControllerToRobot(robot,argumentsController);
		return robot;
	}

	protected void addControllerToRobot(Robot robot, Arguments argumentsControler) {
		robot.setController(controllerFactory.getController(robot, argumentsControler));
	}

	public ArrayList<Robot> getRobots() {
		return robots;
	}

	public void setNNWeights(double[] weights) {
		this.lastWeights = weights;
		for (Robot r : robots) {
			if(r.getEvolvingController() instanceof NeuralNetworkController){
				NeuralNetworkController nnController = (NeuralNetworkController) r.getEvolvingController();
				nnController.setNNWeights(weights);
			}else if(r.getEvolvingController() instanceof BehaviorController){
				BehaviorController bController = (BehaviorController)r.getEvolvingController();
				NeuralNetworkController nnController = (NeuralNetworkController) bController.getEvolvingController();
				nnController.setNNWeights(weights);
			}
		}	
	}

	public void setChromosome(Chromosome chromosome) {
		setNNWeights(chromosome.getAlleles());
	}

	public void endExperiment() {
		hasEnded = true;

		for(Robot r : robots)
			r.getController().end();
	}
	
	public boolean hasEnded() {
		return hasEnded;
	}

	public int getNumberOfStepsPerRun() {
		return numberOfStepsPerRun;
	}

	public void setNumberOfStepsPerRun(int steps) {
		numberOfStepsPerRun = steps;
	}
	
	public String getDescription() {
		return experimentArguments.getArgumentAsStringOrSetDefault("description", "Unknown");
	}
}
