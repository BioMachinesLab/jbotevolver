package simulation.environment;

import gui.renderer.Renderer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.collisionhandling.SimpleCollisionManager;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CollisionManager;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import simulation.util.Factory;

public abstract class Environment implements KeyListener, Serializable {

	private static double MAX_APPROX_SPEED = 0.15;

	protected final int MAXOBJECTS = 5000;
	
	protected ArrayList<Robot> robots = new ArrayList<Robot>(MAXOBJECTS);
	protected ArrayList<Prey>  prey   = new ArrayList<Prey>(MAXOBJECTS);
	protected ArrayList<PhysicalObject> allObjects = new ArrayList<PhysicalObject>(MAXOBJECTS);
	protected ArrayList<PhysicalObject> staticObjects = new ArrayList<PhysicalObject>(MAXOBJECTS);
	protected ArrayList<MovableObject> movableObjects = new ArrayList<MovableObject>(MAXOBJECTS);
	protected ArrayList<PhysicalObject> teleported = new ArrayList<PhysicalObject>(MAXOBJECTS);
	
	protected CollisionManager collisionManager;
	@ArgumentsAnnotation(name="width", defaultValue="4")
	protected double height;
	@ArgumentsAnnotation(name="height", defaultValue="4")
	protected double width;
	@ArgumentsAnnotation(name="steps", defaultValue="100")
	protected int steps;
	
	protected boolean setup = false;

	private GeometricCalculator geometricCalculator;

	public Environment(Simulator simulator, Arguments args) {
		this.width = args.getArgumentAsDoubleOrSetDefault("width", 4);
		this.height = args.getArgumentAsDoubleOrSetDefault("height", 4);
		this.steps = args.getArgumentAsIntOrSetDefault("steps", 100);
		collisionManager = new SimpleCollisionManager(simulator);
		this.geometricCalculator = new GeometricCalculator();//simulator.getGeoCalculator();
	}
	
	public void setup(Simulator simulator) {
		
		/* eliminate the problem where the first random number can be similar even though
		 * the random seed is different (0 to 100 in the case of post-evaluations)
		 * See for more details: http://stackoverflow.com/questions/12282628/why-are-initial-random-numbers-similar-when-using-similar-seeds
		 */
		simulator.getRandom().nextDouble();
		
		this.setup = true;
	}

	public abstract void update(double time);

	public ArrayList<Robot> getRobots() {
		return robots;
	}

	public ArrayList<MovableObject> getMovableObjects() {
		return movableObjects;
	}

	public ArrayList<PhysicalObject> getAllObjects() {
		return allObjects;
	}
	
	public ArrayList<Prey> getPrey() {
		return prey;
	}

	public ArrayList<PhysicalObject> getStaticObjects() {
		return staticObjects;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public GeometricInfo getGeometricInfoBetween(PhysicalObject fromObject,
			PhysicalObject toObject, int time) {
		return geometricCalculator.getGeometricInfoBetween(fromObject,
				toObject, time);
	}

	public ArrayList<PhysicalObject> getTeleported() {
		return teleported;
	}

	public void addTeleported(PhysicalObject object) {
		teleported.add(object);
	}

	public void clearTeleported() {
		teleported.clear();
	}

	public void addRobot(Robot r) {
		robots.add(r);
		addMovableObject(r);
	}

	public void addPrey(Prey p) {
		addMovableObject(p);
		prey.add(p);
	}

	public void addStaticObject(PhysicalObject o) {
		staticObjects.add(o);
		addObject(o);
	}

	public GeometricInfo getGeometricInfoBetween(Vector2d fromPoint,
			double orientation, PhysicalObject toObject, int time) {
		return geometricCalculator.getGeometricInfoBetween(fromPoint,
				orientation, toObject, time);
	}

	public GeometricInfo getGeometricInfoBetweenPoints(Vector2d fromPoint,
			double orientation, Vector2d toPoint, int time) {
		return geometricCalculator.getGeometricInfoBetweenPoints(fromPoint,
				orientation, toPoint, time);
	}
	
	public double getDistanceBetween(Vector2d fromPoint,
			PhysicalObject toObject, int time) {
		return geometricCalculator
				.getDistanceBetween(fromPoint, toObject, time);
	}

	protected void addMovableObject(MovableObject o) {
		movableObjects.add(o);
		addObject(o);
	}
	
	protected void addObject(PhysicalObject physicalObject) {
		allObjects.add(physicalObject);
		teleported.add(physicalObject);
	}
	
	protected void removeObject(PhysicalObject physicalObject) {
		allObjects.remove(physicalObject);
		teleported.remove(physicalObject);
	}

	public void updateCollisions(double time) {
//		 updateRobotCloseObjects(time);
		collisionManager.handleCollisions(this, time);
	}

	public void updateRobotCloseObjects(double time) {
		for (Robot r : robots) {
			r.updateCloseObjects(time, teleported);
		}
	}

	public double getMaxApproximationSpeed() {
		return MAX_APPROX_SPEED;
	}
	
	public int getSteps() {
		return steps;
	}

	public void reset() {
	}

	public void keyPressed(KeyEvent e) {
		for (PhysicalObject o : allObjects) {
			o.keyPressed(e);
		}
	}

	public void keyReleased(KeyEvent e) {
		for (PhysicalObject o : allObjects) {
			o.keyReleased(e);
		}
	}

	public void keyTyped(KeyEvent e) {
		for (PhysicalObject o : allObjects) {
			o.keyTyped(e);
		}
	}

	public void draw(Renderer renderer) {}
	
	public void addRobots(ArrayList<Robot> robots) {
		for(Robot r : robots)
			addRobot(r);
	}
	
	public static Environment getEnvironment(Simulator simulator, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Environment 'classname' not defined: "+ arguments.toString());
		
		return (Environment)Factory.getInstance(arguments.getArgumentAsString("classname"),simulator,arguments);
	}
	
	public boolean isSetup() {
		return setup;
	}
}