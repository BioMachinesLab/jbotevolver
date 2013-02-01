package simulation.environment;

import gui.renderer.Renderer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ArtificialPheromoneObject;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.collisionhandling.SimpleCollisionManager;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CollisionManager;
import simulation.robot.Robot;

public class Environment implements KeyListener, Serializable {

	private static final double MAX_APPROX_SPEED = 0.1;

	public static int MAXOBJECTS = 5000;

	protected static final int MAX_WALLS = 4;
	
	// private static Environment instance;

	protected Simulator simulator;
	protected ArrayList<Robot> robots = new ArrayList<Robot>(MAXOBJECTS);
	protected ArrayList<Prey>  prey   = new ArrayList<Prey>(MAXOBJECTS);
	protected ArrayList<PhysicalObject> allObjects = new ArrayList<PhysicalObject>(MAXOBJECTS);
	protected ArrayList<PhysicalObject> staticObjects = new ArrayList<PhysicalObject>(MAXOBJECTS);
	protected ArrayList<MovableObject> movableObjects = new ArrayList<MovableObject>(MAXOBJECTS);
	protected ArrayList<PhysicalObject> teleported = new ArrayList<PhysicalObject>(MAXOBJECTS);
	protected ArrayList<ArtificialPheromoneObject> artificialPheromones = new ArrayList<ArtificialPheromoneObject>(MAXOBJECTS);
	
	protected CollisionManager collisionManager;
	protected double height;
	protected double width;

	private GeometricCalculator geometricCalculator = new GeometricCalculator();// movableObjects);

	public Environment(Simulator simulator, double width, double height) {
		// if (instance != null)
		// throw new RuntimeException("Already have an environment");
		this.simulator = simulator;
		this.width = width;
		this.height = height;
		collisionManager = new SimpleCollisionManager(simulator);
		// instance=this;
	}

	// public static Environment getInstance() {
	// return instance;
	// }

	// public static void forgetInstance() {
	// instance = null;
	// }

	public void update(int time) {
	}

	public ArrayList<Robot> getRobots() {
		return robots;
	}
	
	public ArrayList<ArtificialPheromoneObject> getPlacedPheromones(){
		return artificialPheromones;
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
	
	public void addPheromone(ArtificialPheromoneObject a){
		addObject(a);
		artificialPheromones.add(a);
	}
	
	public void removePheromones(){
		for(int i= 0; i<artificialPheromones.size(); i++){
			if(artificialPheromones.get(i).getPheromoneIntensity()<0.5)
				artificialPheromones.remove(i);
		}
		
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

	// public void addMovableObject(MovableObject o) {
	protected void addMovableObject(MovableObject o) {
		movableObjects.add(o);
		addObject(o);
	}

	protected void addObject(PhysicalObject physicalObject) {
		allObjects.add(physicalObject);
		teleported.add(physicalObject);
	}

	public void updateCollisions(int time) {
		// updateRobotCloseObjects(time);
		collisionManager.handleCollisions(this, time);
	}

	public void updateRobotCloseObjects(double time) {
		for (Robot r : robots) {
			r.updateCloseObjects(time, teleported);
		}
	}

	public static double getMaxApproximationSpeed() {
		return MAX_APPROX_SPEED;
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

	public void draw(Renderer renderer) {

	}
	
	public int[] getGroundColor(int robotId) {
		return null;
	}

	
}
