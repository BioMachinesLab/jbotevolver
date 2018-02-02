package environment;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class WallsEnvironment extends NestBoundedByWallsEnvironment {
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="3")
	private int numberOfPreys;
	
	private int numberOfFoodSuccessfullyForaged = 0;
	
	private boolean change_PreyInitialDistance;
	
	private Prey prey;

	public WallsEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		if(arguments.getArgumentIsDefined("densityofpreys")){
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 3;
		}
	}
	
	@Override
	public void setup(Simulator simulator) {
			super.setup(simulator);
			for(int i=0; i<numberOfPreys; i++){
				Vector2d position=  newRandomPositionSimplestEnvironment();
				addPrey(new Prey(simulator, "Prey "+i, position, 0, PREY_MASS, PREY_RADIUS));
				wallsForPrey(position);
			}			
	}

	
	private void wallsForPrey(Vector2d position){
		addStaticObject( new Wall( simulator, position.x+0.2, position.y, 0.1, 0.4));
		addStaticObject(new Wall( simulator, position.x-0.2, position.y, 0.1, 0.4));
		addStaticObject(new Wall( simulator, position.x, position.y+ 0.2, 0.4, 0.1));
		addStaticObject(new Wall( simulator, position.x, position.y-0.2, 0.4, 0.1));
	}
	
	@Override
	public void update(double time) {
		change_PreyInitialDistance=false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			 double distance = nextPrey.getPosition().distanceTo(nestPosition);
			 if(nextPrey.isEnabled() && distance < 0.5){
				 Vector2d position= newRandomPositionSimplestEnvironment();
				 nextPrey.teleportTo(position);
				 numberOfFoodSuccessfullyForaged++;
				 prey=nextPrey;
				 change_PreyInitialDistance=true;
			 }
		}		
	}
	
	@Override
	protected void addRobots(){
		for(Robot r: getRobots()){
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}	
	}
	
	private Vector2d newRandomPositionSimplestEnvironment() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+(nestLimit+0.2);
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public boolean isToChange_PreyInitialDistance(){
		return change_PreyInitialDistance;
	}
	
	public Prey preyWithNewDistace(){
		return prey;
	}
}
