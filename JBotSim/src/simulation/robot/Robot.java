package simulation.robot;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import simulation.Simulator;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import controllers.Controller;

/**
 * Representation of a robot, including its physical characteristics such as size 
 * and color, sensors and actuators and the controller.
 * 
 * @author alc
 */
public class Robot extends MovableObject {

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

	public static final int REDINDEX   = 0;
	public static final int GREENINDEX = 1;
	public static final int BLUEINDEX  = 2;
	
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
		
		double relativeX = args.getArgumentAsDoubleOrSetDefault("relativex",0);
		double relativeY = args.getArgumentAsDoubleOrSetDefault("relativey",0);
		double diameter = args.getArgumentAsDoubleOrSetDefault("diameter",0.1);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, relativeX, relativeY, diameter, diameter/2);
		
		double x = args.getArgumentAsDoubleOrSetDefault("x",0);
		double y = args.getArgumentAsDoubleOrSetDefault("y",0);
		setPosition(x, y);
	}
	 public Robot(Simulator simulator, String name, double x, double y, double orientation, double mass, double radius, String color) {
		super(simulator, name, x, y, orientation, mass, PhysicalObjectType.ROBOT);
		this.shape = new CircularShape(simulator, name + "CollisionObject", this, x, y, 2.0 * radius, radius);
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
		for(Actuator actuator: actuators){
			actuator.apply(this);
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
	
	public static ArrayList<Robot> getRobots(Simulator simulator, Arguments arguments) {
		int numberOfRobots = arguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
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
		
		if (!arguments.getArgumentIsDefined("classname")) {
			throw new RuntimeException("Robot 'classname' not defined: "+ arguments.toString());
		}

		String robotName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(robotName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == Simulator.class
						&& params[1] == Arguments.class) {
					return (Robot) constructor.newInstance(simulator,arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown robot: " + robotName);
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
}