
package simulation.robot;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import simulation.util.Factory;
import controllers.Controller;

/**
 * Representation of a robot, including its physical characteristics such as size 
 * and color, sensors and actuators and the controller.
 * 
 * @author alc
 */
public class Robot extends MovableObject {

	private static final long serialVersionUID = -4226242258989368422L;

	/**
	 * All the robots sensors
	 */
	protected ArrayList<Sensor>   sensors   = new ArrayList<Sensor>();

	/**
	 * All the robots actuators
	 */
	protected ArrayList<Actuator> actuators = new ArrayList<Actuator>();
	
	/**
	 * The controller responsible for processing the sensory input and subsequently deciding what actions the robot should take
	 */
	protected Controller controller;
	/**
	 * The color of the robot's body. This color can change during a simulation. COLOR.Black means 
	 * that the robot may be invisible to other nearby robots (unless some sensor/actuator pair that 
	 * does not rely on color is used).
	 * 
	 * The color is defined as an array of three floats for speed.
	 */
	protected double[]      bodyColor  = new double[3];
	
	@ArgumentsAnnotation(name="description", defaultValue = "robot")
	protected String description = "";
	@ArgumentsAnnotation(name="radius", defaultValue = "0.05")
	private double radius;
	@ArgumentsAnnotation(name="diameter", defaultValue = "0.1")
	private double diameter;
	@ArgumentsAnnotation(name = "relativex", defaultValue = "0")
	private double relativeX;
	@ArgumentsAnnotation(name = "relativey", defaultValue = "0")
	private double relativeY;
	@ArgumentsAnnotation(name = "x", defaultValue = "0")
	private double x;
	@ArgumentsAnnotation(name = "y", defaultValue = "0")
	private double y;
	@ArgumentsAnnotation(name = "color", values={"black","blue","cyan","dark gray","gray"
			,"green","light gray","magneta","orange","pink","red","white","yellow"})
	private Color color;
	
	@ArgumentsAnnotation(name="variablenumber", values={"0","1"})
	private static int variableNumber;
	
	@ArgumentsAnnotation(name="numberofrobots", defaultValue = "1")	
	private Color ledColor;
	private LedState ledState;
	
	public static final int REDINDEX   = 0;
	public static final int GREENINDEX = 1;
	public static final int BLUEINDEX  = 2;
	
	@ArgumentsAnnotation(name="ignoredisabledsensors", values={"0","1"})
	protected boolean ignoreDisabledSensors = false;
	
	@ArgumentsAnnotation(name="specialwallcollisions", values={"0","1"})
	protected boolean specialWallCollisions = false;
	@ArgumentsAnnotation(name="ignorerobottorobotcollisions", values={"0","1"})
	protected boolean ignoreRobotToRobotCollisions = false;
	
	protected LinkedList<PhysicalObject> collidingObjects = new LinkedList<PhysicalObject>();
	
	/**
	 * Initialize a new robot.
	 * 
	 * @param name human readable name (for debugging and logging purposes)
	 * @param x the x-coordinate of the robot's start location
	 * @param y the y-coordinate of the robot's start location
	 * @param orientation the robot's start orientation
	 * @param mass the mass of the robot
	 * @param radius the radius of the robot
	 * @param color 
	 */
	public Robot(Simulator simulator, Arguments args) {
		super(simulator, args);
		relativeX = args.getArgumentAsDoubleOrSetDefault("relativex",0);
		relativeY = args.getArgumentAsDoubleOrSetDefault("relativey",0);
		radius = args.getArgumentAsDoubleOrSetDefault("radius",0.05);
		diameter = args.getArgumentAsDoubleOrSetDefault("diameter",radius*2);
		ignoreDisabledSensors = args.getArgumentAsIntOrSetDefault("ignoredisabledsensors",0) == 1; 
		ignoreRobotToRobotCollisions = args.getArgumentAsIntOrSetDefault("ignorerobottorobotcollisions",0) == 1; 
		if(diameter != radius*2)
			radius = diameter/2;
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, relativeX, relativeY, diameter, diameter/2);

		this.description = args.getArgumentAsStringOrSetDefault("description", "robot");
		
		x = args.getArgumentAsDoubleOrSetDefault("x",0);
		y = args.getArgumentAsDoubleOrSetDefault("y",0);
		setPosition(x, y);

		try {
		    Field field = Color.class.getField(args.getArgumentAsStringOrSetDefault("color", "black"));
		    color = (Color)field.get(null);
		    ledColor =  (Color)field.get(null); 
		} catch (Exception e) {
		    color = null; // Not defined
		    ledColor = null;
		}
		
		ledState = LedState.OFF;
		
		if(color != null)
			setBodyColor(color);
		
		specialWallCollisions = args.getArgumentAsIntOrSetDefault("specialwallcollisions",0) == 1; 
	}
	
	/**
	 * Get the controller of a robot.
	 * @return the reference to the controller for the robot (or null)
	 */
	
	public Controller getController() {
		return controller;
	}
	
	/**
	 * Get a list of the robot's sensors.
	 * 
	 * @return the robot's sensors.
	 */	
	public ArrayList<Sensor> getSensors() {
		return sensors;
	}

	/**
	 * Get a sensor with a specific id. The id of sensors and actuators are usually specified in a configuration file.
	 * 
	 * @param id the id of a sensor
	 * @return the sensor with the id specified. If no such sensor is found, a {@link RuntimeException} is thrown.
	 * 
	 */
	public Sensor getSensorWithId(int id) {
		boolean found  = false;
		Sensor  sensor = null;
		Iterator<Sensor> i = sensors.iterator();
		
		while (!found && i.hasNext()) {
			sensor = i.next();
			if (id == sensor.getId()) {
				found = true;
			}
		}
		
		if (!found) {
			throw new RuntimeException("Cannot find sensor with id: " + id + ". (" + sensors.size() + " sensors available)");
		}
		
		return sensor;
	}
	
	/**
	 * Add a sensor to the robot
	 * 
	 * @param sensor the new sensor to associate to the robot
	 */
	
	public void addSensor(Sensor sensor) {
		sensors.add(sensor);
	}
	
	/**
	 * Get a list of actuators for the robot.
	 * 
	 * @return a list of the robot's actuators
	 */
	public ArrayList<Actuator> getActuators() {
		return actuators;
	}
	
	/**
	 * Get an actuator with a specific id. The id of sensors and actuators are usually specified in a configuration file.
	 * 
	 * @param id the id of the actuator.
	 * @return the actuator with the specified id.
	 */
	public Actuator getActuatorWithId(int id) {
		boolean  found    = false;
		Actuator actuator = null;
		Iterator<Actuator> i = actuators.iterator();
		
		while (!found && i.hasNext()) {
			actuator = i.next();
			if (id == actuator.getId()) {
				found = true;
			}
		}
		
		if (!found) {
			throw new RuntimeException("Cannot find actuator with id: " + id + ". (" + actuators.size() + " actuators available)");
		}
		return actuator;
	}
	
	/**
	 * Add a new actuator to the robot.
	 * 
	 * @param actuator the new actuator to add to the robot.
	 */	
	public void addActuator(Actuator actuator) {
		actuators.add(actuator);
	}
	
	/**
	 * Get the robot's body color.
	 * 
	 * @return The color of the robot's body.
	 */
	public Color getBodyColor() {
		return new Color((float) bodyColor[REDINDEX], (float) bodyColor[GREENINDEX], (float) bodyColor[BLUEINDEX]);
	}

	/**
	 * Set the robot's body color.
	 * 
	 * @param color new color of a robot's body.
	 */
	public void setBodyColor(Color color) {
		this.bodyColor[REDINDEX]    = ((double) color.getRed()   / 255.0f) ;
		this.bodyColor[GREENINDEX]  = ((double) color.getGreen() / 255.0f) ;
		this.bodyColor[BLUEINDEX]   = ((double) color.getBlue()  / 255.0f) ;
	}
	
	/**
	 * Update all sensor readings.
	 * 
	 * @param simulationStep the number of the current simulation step
	 * @param teleported     the list of teleported objects
	 * 
	 */
	public void updateSensors(double simulationStep, ArrayList<PhysicalObject> teleported) {
		for(Sensor sensor : sensors){
			if(!ignoreDisabledSensors || sensor.isEnabled())
				sensor.update(simulationStep,teleported);
		}
	}
	
	/**
	 * Update all actuators including moving the robot (using differential drive kinematics) according to the speeds 
	 * of its left and right wheels.
	 * 
	 * @param time the number of the current simulation step.
	 * @param timeDelta      the time (in virtual seconds) between calls to this method. 
	 */
	public void updateActuators(Double time, double timeDelta) {
		this.previousPosition = new Vector2d(position);
		for(Actuator actuator: actuators){
			actuator.apply(this, timeDelta);
		}
	}
		
	/**
	 * TODO: Sancho
	 * @param simulationStep
	 * @param teleported
	 */
	
	public void updateCloseObjects(Double simulationStep,
			ArrayList<PhysicalObject> teleported) {
		shape.getCloseRobot().update(simulationStep, teleported);		
	}

	/**
	 * Stops the robot in case it is moving
	 */
	public void stop() {}
	
	/**
	 * Set the controller for the robot
	 * 
	 * @param controller the new controller for the robot.
	 */
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	/**
	 * Process key presses: forward keyboard events to sensors, actuator and controllers so that they can respond to it 
	 * (mostly for debugging purposes and for interactive experiments).
	 */	
	public void keyPressed(KeyEvent e) {
		for (Sensor s : sensors) {
			s.keyPressed(e);
		}

		for (Actuator a : actuators) {
			a.keyPressed(e);
		}

		if(controller != null)
			controller.keyPressed(e);
	}

	/**
	 * Process key releases: forward keyboard events to sensors, actuator and controllers so that they can respond to it 
	 * (mostly for debugging purposes and for interactive experiments).
	 */
	public void keyReleased(KeyEvent e) {
		for (Sensor s : sensors) {
			s.keyReleased(e);
		}

		for (Actuator a : actuators) {
			a.keyReleased(e);
		}
		
		if(controller != null)
			controller.keyReleased(e);
	}

	/**
	 * Process key input: forward keyboard events to sensors, actuator and controllers so that they can respond to it 
	 * (mostly for debugging purposes and for interactive experiments).
	 */
	public void keyTyped(KeyEvent e) {
		for (Sensor s : sensors) {
			s.keyTyped(e);
		}

		for (Actuator a : actuators) {
			a.keyTyped(e);
		}

		if(controller != null)
			controller.keyTyped(e);
	}

	/**
	 * Set the color of a robot's body using RGB values between 0..1.0 for red, green and blue. 
	 *
	 * @param red   the value for red.
	 * @param green the value for green.
	 * @param blue  the value for blue.
	 * 
	 **/
	public void setBodyColor(double red, double green, double blue) {
		bodyColor[REDINDEX]    = red;
		bodyColor[GREENINDEX]  = green;
		bodyColor[BLUEINDEX]   = blue;
	}

	/**
	 * Set the color of a robot's body using RGB values between 0..1.0 for red, green and blue. 
	 *
	 * @param color array with length three.
	 */
	
	public void setBodyColor(double[] color) {
		this.bodyColor = color;
	}

	/**
	 * Get the color of a robot's body as three doubles (one for red, green and blue, respectively)
	 * 
	 * @return robot's body in three doubles each between 0..1.0 for red, green and blue, respectively.
	 */
	public double[] getBodyColorAsDoubles() {
		return this.bodyColor;
	}
	
	public String getDescription() {
		return description;
	}

	public Sensor getSensorByType(Class sensorClass){
		for(Sensor s : sensors){
			if(s.getClass().equals(sensorClass))
				return s;
		}
		return null;
	}
	
	public Actuator getActuatorByType(Class actuatorClass){
		for(Actuator a : actuators){
			if(a.getClass().equals(actuatorClass))
				return a;
		}
		return null;
	}
	
	public LinkedList<PhysicalObject> getCollidingObjects() {
		return collidingObjects;
	}
	
	public static ArrayList<Robot> getRobots(Simulator simulator, Arguments arguments) {
		int numberOfRobots = arguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
		variableNumber = arguments.getArgumentAsIntOrSetDefault("variablenumber", 0);
		
		if(variableNumber == 1) {
			numberOfRobots = simulator.getRandom().nextInt(numberOfRobots) + 1;
		}
		
		
		if(arguments.getArgumentIsDefined("randomizenumber")) {		
			String[] rawArray = arguments.getArgumentAsString("randomizenumber").split(",");
			
			if(rawArray.length > 1)
				numberOfRobots = Integer.parseInt(rawArray[simulator.getRandom().nextInt(rawArray.length)]);
		}
		
		if(arguments.getArgumentIsDefined("totalrobots")) {		
			int totalRobots = arguments.getArgumentAsInt("totalrobots");
			int previousNumberOfRobots = arguments.getArgumentAsIntOrSetDefault("previousrobots", 0);
			numberOfRobots = totalRobots - previousNumberOfRobots;
		}
		
		if(arguments.getArgumentIsDefined("randomize")) {
			int extra = simulator.getRandom().nextInt(arguments.getArgumentAsInt("randomize")*2)-arguments.getArgumentAsInt("randomize");
			numberOfRobots+=extra;
		}
		
		ArrayList<Robot> robots = new ArrayList<Robot>(numberOfRobots);
		for(int i = 0 ; i < numberOfRobots ; i++)
			robots.add(getRobot(simulator, arguments));
		return robots;
	}

	public static Robot getRobot(Simulator simulator, Arguments arguments) {
		Robot robot = createRobot(simulator, arguments);

		addSensors(simulator, robot, arguments);
		addActuators(simulator, robot, arguments);

		return robot;
	}

	private static Robot createRobot(Simulator simulator, Arguments arguments) {
		
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Robot 'classname' not defined: "+ arguments.toString());

		return (Robot)Factory.getInstance(arguments.getArgumentAsString("classname"), simulator,arguments);
	}

	private static void addSensors(Simulator simulator, Robot robot, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("sensors"))
			return;

		Arguments sensors = new Arguments(arguments.getArgumentAsString("sensors"));

		for (int i = 0; i < sensors.getNumberOfArguments(); i++) {
			Arguments sensorArgs = new Arguments(sensors.getValueAt(i));
			Sensor sensor = Sensor.getSensor(robot, simulator,sensorArgs.getArgumentAsString("classname"),sensorArgs);
			robot.addSensor(sensor);
		}
	}

	private static void addActuators(Simulator simulator, Robot robot, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("actuators"))
			return;

		Arguments actuators = new Arguments(arguments.getArgumentAsString("actuators"));

		for (int i = 0; i < actuators.getNumberOfArguments(); i++) {
			Arguments actuatorArgs = new Arguments(actuators.getValueAt(i));
			Actuator actuator = Actuator.getActuator(simulator, actuatorArgs.getArgumentAsString("classname"), actuatorArgs);
			robot.addActuator(actuator);
		}
	}
	
	public boolean specialWallCollisions() {
		return specialWallCollisions;
	}
	
	public boolean ignoreRobotToRobotCollisions() {
		return ignoreRobotToRobotCollisions;
	}
	
	public Color getLedColor() {
		return ledColor;
	}
	
	public void setLedColor(Color ledColor) {
		this.ledColor = ledColor;
	}
	
	public LedState getLedState() {
		return ledState;
	}
	
	public void setLedState(LedState ledState) {
		this.ledState = ledState;
	}
	
	public boolean ignoreWallCollisions() {
		return false;
	}
	
}