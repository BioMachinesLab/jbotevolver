package simulation.environment;

import gui.renderer.Renderer;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class RoundForageEnvironment extends Environment implements NestEnvironment{

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private	double forbiddenArea;
	private int    numberOfPreys;
	private Nest   nest;
	private int    numberOfFoodSuccessfullyForaged = 0;


	public RoundForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		nestLimit       = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")       : .5;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")       : 2.0;
		forbiddenArea   = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")       : 5.0;
		
		if(arguments.getArgumentIsDefined("densityofpreys")){
			double densityoffood       = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
		}
				
		for(int i = 0; i < numberOfPreys; i++ ){
			addPrey(new Prey(simulator, "Prey "+i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
	}

	private Vector2d newRandomPosition() {
		double radius=simulator.getRandom().nextDouble()*(forageLimit-nestLimit)+nestLimit;
		double angle=simulator.getRandom().nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	@Override
	public void update(double time) {
		nest.shape.getClosePrey().update(time, teleported);
		CloseObjectIterator i = nest.shape.getClosePrey().iterator();

		while(i.hasNext()){
			 PhysicalObjectDistance preyDistance = i.next();
			 Prey nextPrey = (Prey)(preyDistance.getObject());
			 double distance = nextPrey.getPosition().length();
			 if(nextPrey.isEnabled() && distance < nestLimit){
				 if(distance == 0){
					 System.out.println("ERRO--- zero");
				 }
				 nextPrey.teleportTo(newRandomPosition());
				 numberOfFoodSuccessfullyForaged++;
			 }
			 i.updateCurrentDistance(distance);
		}
		
		for(Robot robot: robots){
			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class.getName());
			if (sensor.preyCarried() && robot.isInvolvedInCollison()){
				PreyPickerActuator actuator = (PreyPickerActuator)robot.getActuatorByType(PreyPickerActuator.class.getName());
				Prey preyToDrop = actuator.dropPrey();
				preyToDrop.teleportTo(newRandomPosition());
			}
		}
	}
	
	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public double getNestRadius() {
		return nestLimit;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	@Override
	public void draw(Renderer renderer){
		renderer.drawCircle(nest.getPosition(), forageLimit);
		renderer.drawCircle(nest.getPosition(), forbiddenArea);
	}
}
