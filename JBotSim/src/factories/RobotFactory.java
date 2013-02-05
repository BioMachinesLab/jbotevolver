package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ClassSearchUtils;

public class RobotFactory extends Factory implements Serializable {
	
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

	public static Robot getRobotFromTeam(Simulator simulator, Arguments arguments, int team) {
		Robot robot = createRobot(simulator, arguments);
		robot.setParameter("TEAM", team);

		addSensors(simulator, robot, arguments);
		addActuators(simulator, robot, arguments);
		
		return robot;
	}

	private static Robot createRobot(Simulator simulator, Arguments arguments) {
		
		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("Robot 'name' not defined: "+ arguments.toString());
		}

		String robotName = arguments.getArgumentAsString("name");

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

	public static void addSensors(Simulator simulator, Robot robot, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("sensors"))
			return;

		Arguments sensors = new Arguments(
				arguments.getArgumentAsString("sensors"));

		for (int i = 0; i < sensors.getNumberOfArguments(); i++) {
			Sensor sensor = createSensor(i, robot, simulator,sensors.getArgumentAt(i),new Arguments(sensors.getValueAt(i)));
			robot.addSensor(sensor);
		}
	}

	public static void addActuators(Simulator simulator, Robot robot, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("actuators"))
			return;

		Arguments actuators = new Arguments(
				arguments.getArgumentAsString("actuators"));

		for (int i = 0; i < actuators.getNumberOfArguments(); i++) {
			Actuator actuator = createActuator(simulator, i, robot, actuators.getArgumentAt(i),new Arguments(actuators.getValueAt(i)));
			robot.addActuator(actuator);
		}
	}

	public static Sensor createSensor(int id, Robot robot, Simulator simulator, String name, Arguments arguments) {
		if (arguments.getArgumentIsDefined("id"))
			id = arguments.getArgumentAsInt("id");
		
		name = ClassSearchUtils.getClassFullName(name);
		
		try {
			Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 4 && params[0] == Simulator.class && params[1] == int.class
						&& params[2] == Robot.class && params[3] == Arguments.class)
					return (Sensor) constructor.newInstance(simulator,id,robot,arguments);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown sensor: " + name);
	}

	public static Actuator createActuator(Simulator simulator, int id, Robot robot, String name, Arguments arguments) {
		if (arguments.getArgumentIsDefined("id"))
			id = arguments.getArgumentAsInt("id");
		
		name = ClassSearchUtils.getClassFullName(name);
		
		try {
			Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 3 && params[0] == Simulator.class &&
						params[1] == int.class &&
						params[2] == Arguments.class)
					return (Actuator) constructor.newInstance(simulator,id,arguments);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown actuator: " + name);
	}
}