package simulation.environment;

import java.awt.Color;
import java.io.File;
import java.io.FilePermission;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.BeeRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import gui.renderer.Renderer;

/**
 * This environment treats robots as bees. The robots have an energy
 * level, which they can recharge in the nest/hive. If the robot runs
 * out of energy, it dies. It is also possible to generate new bees
 * if they forage enough food.
 * 
 * @author miguelduarte
 */
public class BeeEnvironment extends Environment {

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
	private int     redeployCounter;
	private int     currentRedeployCounter;
	private boolean preyIncreaseDistance;
	private int     amountOfFood;
	private Random  random;
	private Nest    nest;
	private int    numberOfFoodSuccessfullyForaged = 0;
	private double beeInitialEnergy;
	private double hiveInitialEnergy;
	private double beeGenerationThreshold;
	private double beeGenerationEnergy;
	private double hiveCurrentEnergy;
	private double hiveEnergyGain;
	private double beeEnergyConsumption;
	private double beeEnergyGain;
	private double beeMaxEnergy;
	private Random preyPlacementRandom;
	private boolean firstUpdate = true;
	private int		deadBees = 0;
	private boolean energyOnlyInNest;
	private double totalColor = 0;
	private FileWriter statsFile;

	public BeeEnvironment(Simulator simulator, double width, double height) {
		super(simulator, width, height);
		random = simulator.getRandom();
		preyPlacementRandom = new Random(random.nextLong());
	}

	public BeeEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")				: 7, 
				arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")				: 7);

		random = simulator.getRandom();
		preyPlacementRandom = new Random(random.nextLong());

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

		//Bee variables
		beeInitialEnergy    = arguments.getArgumentIsDefined("beeinitialenergy") ? arguments.getArgumentAsDouble("beeinitialenergy")	: 0;
		hiveInitialEnergy    = arguments.getArgumentIsDefined("hiveinitialenergy") ? arguments.getArgumentAsDouble("hiveinitialenergy")	: 0;
		beeGenerationThreshold    = arguments.getArgumentIsDefined("beegenerationthreshold") ? arguments.getArgumentAsDouble("beegenerationthreshold")	: 0;
		beeGenerationEnergy    = arguments.getArgumentIsDefined("beegenerationenergy") ? arguments.getArgumentAsDouble("beegenerationenergy")	: 0;
		beeEnergyConsumption    = arguments.getArgumentIsDefined("beeenergyconsumption") ? arguments.getArgumentAsDouble("beeenergyconsumption")	: 0;
		beeEnergyGain		    = arguments.getArgumentIsDefined("beeenergygain") ? arguments.getArgumentAsDouble("beeenergygain")	: 0;
		beeMaxEnergy		    = arguments.getArgumentIsDefined("beemaxenergy") ? arguments.getArgumentAsDouble("beemaxenergy")	: 1;
		hiveEnergyGain		    = arguments.getArgumentIsDefined("hiveenergygain") ? arguments.getArgumentAsDouble("hiveenergygain")	: 0;
		energyOnlyInNest		= arguments.getArgumentIsDefined("energyonlyinnest") ? (arguments.getArgumentAsInt("energyonlyinnest")==1)	: false;

		if(redeployPrey)
			currentRedeployCounter = redeployCounter;

		//Position initial prey
		preyCenter = calculatePreyCenter();

		for(int i = 0; i < amountOfFood; i++ ){
			addPrey(new Prey(simulator, "Prey "+i, newPreyPosition(), 0, PREY_MASS, PREY_RADIUS));
		}

		hiveCurrentEnergy = hiveInitialEnergy;

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
	public void update(int time) {
		super.update(time);

		if(firstUpdate){
			//Set robot energy level
			for(Robot r : robots) {
				BeeRobot bee = (BeeRobot) r;
				bee.setEnergy(beeInitialEnergy);
				bee.setMaxEnergy(beeMaxEnergy);
			}
			firstUpdate = false;
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

				if(redeployPrey && currentRedeployCounter <= 0){
					nextPrey.setCarrier(null);
					nextPrey.setEnabled(false);
				}
				else if(redeployPrey){
					nextPrey.teleportTo(newPreyPosition());
					currentRedeployCounter--;
				}else
					nextPrey.teleportTo(newPreyPosition());

				numberOfFoodSuccessfullyForaged++;

				//Increase hive energy
				hiveCurrentEnergy+=hiveEnergyGain;

				if(hiveCurrentEnergy > beeGenerationThreshold){
					hiveCurrentEnergy -= beeGenerationEnergy;
					createNewRobot();
				}
			}
			i.updateCurrentDistance(distance);

		}

		for(Robot robot: robots){

			if (robot.isCarryingPrey() && robot.isInvolvedInCollison()){
				Prey preyToDrop = robot.dropPrey();
				preyToDrop.teleportTo(newPreyPosition());
			}

			//Handle energy
			BeeRobot bee = (BeeRobot)robot;
			double currentEnergy = bee.getEnergy();
			double decreasedEnergy = currentEnergy - beeEnergyConsumption;
			double increasedEnergy = currentEnergy + beeEnergyGain;

			double distanceToNest = Math.abs(bee.getPosition().length() - nest.getPosition().length());

			//We decrease the bee's energy if
			// -it's not carrying a prey
			// -it isn't on the nest (depends on the configuration) 
			//Increase the energy if it's resting on the nest
			if(currentEnergy > 0 && (!bee.isCarryingPrey() || energyOnlyInNest) && distanceToNest > nestLimit){
				bee.setEnergy(decreasedEnergy);
			} else if(distanceToNest < nestLimit) {
				bee.setEnergy(increasedEnergy);
			}

			if(bee.getEnergy() > beeMaxEnergy){
				bee.setEnergy(beeMaxEnergy);
			}else if(bee.getEnergy() <= 0 && bee.isEnabled()){
				bee.setEnergy(0);
				bee.setEnabled(false);
			}

		}

		if(redeployPrey && !anyActivePrey()){ 
			//There are no prey active, so we have to redeploy
			redeployPrey();
		}
		removeDeadRobots();
	}

	private double[] statsLightSensors(Robot r){

		double maxNest = 0; double currentNest = 0;
		double maxPrey = 0; double currentPrey = 0;

		//1=nest, 2=prey, 4=color

		for(int i = 0 ; i < 8 ; i++){
			currentNest = r.getSensorWithId(1).getSensorReading(i);
			if(currentNest > maxNest) maxNest = currentNest;

			currentPrey = r.getSensorWithId(2).getSensorReading(i);
			if(currentPrey > maxPrey) maxPrey = currentPrey;
		}

		double robotFrontDist = r.getSensorWithId(4).getSensorReading(0);
		double robotBackDist = r.getSensorWithId(4).getSensorReading(4);
		double robotFrontColor = 0;//r.getSensorWithId(4).getSensorReading(8);
		double robotBackColor = 0;//r.getSensorWithId(4).getSensorReading(12);

		double[] result = {maxNest,maxPrey,robotFrontDist,robotFrontColor,robotBackDist, robotBackColor};
		return result;
	}

	private int[] statsNest(){

		int in = 0;
		int out = 0;

		for(Robot r : robots){
			double distanceToNest = Math.abs(r.getPosition().length() - nest.getPosition().length());
			if(distanceToNest < nestLimit)
				in++;
			else
				out++;
		}

		int[] result = {in,out};

		return result;
	}

	private void removeDeadRobots(){
		Iterator<Robot> i = robots.iterator();
		while(i.hasNext()){
			Robot r = i.next();
			if(!r.isEnabled()){	
				deadBees++;
				r.setBodyColor(Color.ORANGE);
				if(r.isCarryingPrey())
					r.dropPrey();
				i.remove();
			}
		}
	}

	private boolean anyActivePrey(){

		Iterator<Prey> iterator = getPrey().iterator();

		boolean activePrey = false;

		while(iterator.hasNext() && !activePrey){
			Prey p = iterator.next();
			activePrey = p.isEnabled() || p.getHolder() != null;
		}

		return activePrey;
	}

	private void redeployPrey(){

		preyAngle = newRandomAngle();
		currentRedeployCounter = redeployCounter;
		preyCenter = calculatePreyCenter();

		for(Prey p: getPrey()){
			p.setEnabled(true);
			p.teleportTo(newPreyPosition());
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

	private void createNewRobot(){
		simulator.getExperiment().createOneRobot();

		BeeRobot bee = (BeeRobot)robots.get(robots.size()-1);
		bee.setEnergy(beeInitialEnergy);
		bee.setMaxEnergy(beeMaxEnergy);
	}

	public int deadBees(){
		return deadBees;
	}

	@Override
	public void draw(Renderer renderer){
		renderer.drawCircle(nest.getPosition(), forageLimit);
		renderer.drawCircle(nest.getPosition(), forbiddenArea);
	}
}