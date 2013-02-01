package simulation.environment;


import gui.renderer.Renderer;

import java.util.Iterator;
import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class GroupedPreyEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private	double forbiddenArea;
	private	double preyAngle;
	private double preyMinDistance;
	private double preyMaxDistance;
	private double preyCenter;
	private int    totalSamples;
	private int    currentSample;
	private boolean redeployPrey;
	private int redeployCounter;
	private int currentRedeployCounter;
	private boolean preyIncreaseDistance;
	private int    amountOfFood;
	private Nest   nest;
	private int    numberOfFoodSuccessfullyForaged = 0;
	private Random preyPlacementRandom;

	public GroupedPreyEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		preyPlacementRandom = new Random(simulator.getRandom().nextLong());

		forbiddenArea       = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")	: 7;
		nestLimit       = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")			: .5;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")		: 6.0;
		preyAngle    = arguments.getArgumentIsDefined("preyangle") ? arguments.getArgumentAsDouble("preyangle")	: newRandomAngle();
		preyMinDistance    = arguments.getArgumentIsDefined("preymindistance") ? arguments.getArgumentAsDouble("preymindistance")	: 3;
		preyMaxDistance    = arguments.getArgumentIsDefined("preymaxdistance") ? arguments.getArgumentAsDouble("preymaxdistance")	: 6;
		amountOfFood    = arguments.getArgumentIsDefined("amountoffood") ? arguments.getArgumentAsInt("amountoffood")	: 10;
		preyIncreaseDistance    = arguments.getArgumentIsDefined("preyincreasedistance") ? (arguments.getArgumentAsInt("preyincreasedistance")==1)	: false;
		totalSamples    = arguments.getArgumentIsDefined("totalsamples") ? arguments.getArgumentAsInt("totalsamples")	: 5;
		currentSample    = arguments.getArgumentIsDefined("fitnesssample") ? arguments.getArgumentAsInt("fitnesssample")	: 0;
		redeployPrey    = arguments.getArgumentIsDefined("redeployprey") ? (arguments.getArgumentAsInt("redeployprey")==1)	: false;
		redeployCounter    = arguments.getArgumentIsDefined("redeploycounter") ? arguments.getArgumentAsInt("redeploycounter")	: 0;
		
		if(redeployPrey)
			currentRedeployCounter = redeployCounter;
	}
	
	@Override
	public void setup() {
		preyCenter = calculatePreyCenter();
		
		for(int i = 0; i < amountOfFood; i++ ){
			addPrey(new Prey(simulator,"Prey "+i, newPreyPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
		
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
	}
	
	private double newRandomAngle(){
		double r = preyPlacementRandom.nextDouble()*2*Math.PI;
		return r;
	}
	
	private double calculatePreyCenter(){
		double distanceGap = preyMaxDistance - preyMinDistance;
		double randomInGap = preyPlacementRandom.nextDouble()*distanceGap;
		double preyCenter = preyMinDistance + randomInGap;
		
		if(preyIncreaseDistance)
			preyCenter = preyMinDistance + (distanceGap/(totalSamples-1))*currentSample;
		
		return preyCenter;
	}

	private Vector2d newPreyPosition() {			
		return new Vector2d(preyCenter*Math.cos(preyAngle),preyCenter*Math.sin(preyAngle));
	}
	
	@Override
	public void update(double time) {
		if(redeployPrey && !anyActivePrey()){ 
			//There are no prey active, so we have to redeploy
			redeployPrey();
		}
		
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
				 
				 if(redeployPrey && currentRedeployCounter <= 0)
					 nextPrey.setEnabled(false);
				 else if(redeployPrey){
					 nextPrey.teleportTo(newPreyPosition());
					 currentRedeployCounter--;
				 }else
					 nextPrey.teleportTo(newPreyPosition());
				 
				 numberOfFoodSuccessfullyForaged++;
			 }
			 i.updateCurrentDistance(distance);
		}
		
		for(Robot robot: robots){
			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class.getName());
			if (sensor.preyCarried() && robot.isInvolvedInCollison()){
				PreyPickerActuator actuator = (PreyPickerActuator)robot.getActuatorByType(PreyPickerActuator.class.getName());
				Prey preyToDrop = actuator.dropPrey();
				preyToDrop.teleportTo(newPreyPosition());
			}
		}
	}
	
	private boolean anyActivePrey(){
		
		Iterator<Prey> iterator = getPrey().iterator();
		
		boolean activePrey = false;
		
		while(iterator.hasNext() && !activePrey){
			activePrey = iterator.next().isEnabled();
		}
		
		return activePrey;
	}
	
	private void redeployPrey(){
		
		getPrey().clear();
		
		preyAngle = newRandomAngle();
		currentRedeployCounter = redeployCounter;
		preyCenter = calculatePreyCenter();
		
		for(int prey = 0; prey < amountOfFood; prey++ ){
			addPrey(new Prey(simulator, "Prey "+prey, newPreyPosition(), 0, PREY_MASS, PREY_RADIUS));
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