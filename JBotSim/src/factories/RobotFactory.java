package factories;

import java.io.Serializable;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.ARDrone;
import simulation.robot.BeeRobot;
import simulation.robot.MultiPreyForagerRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.BehaviorActuator;
import simulation.robot.actuators.MultiPreyPickerActuator;
import simulation.robot.actuators.OpenDoorActuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.robot.actuators.SimplePreyPickerActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.BehaviorSensor;
import simulation.robot.sensors.EpuckLightSensor;
import simulation.robot.sensors.NestSensor;
import simulation.robot.sensors.SimpleNestSensor;
import simulation.robot.sensors.SimplePreySensor;
import simulation.robot.sensors.SimplePreySensorPerimeter;
import simulation.robot.sensors.WallButtonSensor;
import simulation.robot.sensors.XRayPreySensor;
import simulation.robot.sensors.CompassSensor;
import simulation.robot.sensors.DoubleParameterSensor;
import simulation.robot.sensors.EnergySensor;
import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.GroundRGBColorSensor;
import simulation.robot.sensors.InNestSensor;
import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.MultyPreyCarriedSensor;
import simulation.robot.sensors.NearRobotSensor;
import simulation.robot.sensors.PerimeterSimpleLightTypeSensor;
import simulation.robot.sensors.PerimeterSimpleRobotColorSensor;
import simulation.robot.sensors.PheromoneSensor;
import simulation.robot.sensors.PositionSensor;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.PreySensor;
import simulation.robot.sensors.RobotColorSensor;
import simulation.robot.sensors.RobotRGBColorSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.SimpleLightTypeSensor;
import simulation.robot.sensors.SimpleRobotColorSensor;
import simulation.util.Arguments;
import experiments.Experiment;

public class RobotFactory extends Factory implements Serializable {

	private int numberOfRobots = 0;

	public RobotFactory(Simulator simulator) {
		super(simulator);
	}

	public Robot getRobot(Arguments arguments) {
		Robot robot = createRobot(arguments);

		addSensors(robot, arguments);
		addActuators(robot, arguments);

		return robot;
	}

	public Robot getRobotFromTeam(Arguments arguments, 
			int team) {
		Robot robot = createRobot(arguments);
		robot.setParameter("TEAM", team);

		addSensors(robot, arguments);
		addActuators(robot, arguments);
		
		return robot;
	}

	private Robot createRobot(Arguments arguments) {
		if (arguments == null) {
			arguments = new Arguments("name=differentialdrive");
		}

		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("Robot 'name' not defined: "
					+ arguments.toString());
		}

		String robotName = arguments.getArgumentAsString("name");

		Robot robot = null;
		if (robotName.equalsIgnoreCase("differentialdrive")
				|| (robotName.equalsIgnoreCase("bee"))
				|| (robotName.equalsIgnoreCase("MultiPreyForagerRobot"))
				|| (robotName.equalsIgnoreCase("ardrone"))) {
			Vector2d position = arguments.getArgumentIsDefined("position") ? arguments
					.getArgumentAsVector2d("position") : new Vector2d(0, 0);
			double orientation = arguments.getArgumentIsDefined("orientation") ? arguments
					.getArgumentAsDouble("orientation") : simulator.getRandom()
					.nextGaussian() * Math.PI;
			double mass = arguments.getArgumentIsDefined("mass") ? arguments
					.getArgumentAsDouble("mass") : 1.0;
			double radius = arguments.getArgumentIsDefined("radius") ? arguments
					.getArgumentAsDouble("radius") : 0.1;
			double extraRadius = arguments.getArgumentIsDefined("extraradius") ? arguments
							.getArgumentAsDouble("extraradius") : 0;
			double distanceWheels = arguments.getArgumentIsDefined("distancewheels") ? arguments
					.getArgumentAsDouble("distancewheels") : radius*2;
			String color = arguments.getArgumentIsDefined("color") ? arguments
					.getArgumentAsString("color") : "black";

			if (robotName.equalsIgnoreCase("differentialdrive"))
				robot = new Robot(simulator, robotName + numberOfRobots++,
						position.x, position.y, orientation, mass, radius,distanceWheels,color);
			
			if (robotName.equalsIgnoreCase("ardrone")) 
				robot = new ARDrone(simulator, robotName + numberOfRobots++,
						position.x, position.y, orientation, mass, radius,distanceWheels,color);
			
			if (robotName.equalsIgnoreCase("bee"))
				robot = new BeeRobot(simulator, robotName + numberOfRobots++,
						position.x, position.y, orientation, mass, radius, distanceWheels,
						color);
			if (robotName.equalsIgnoreCase("MultiPreyForagerRobot")) {
				int limit = arguments.getArgumentIsDefined("preyslimit") ? arguments
						.getArgumentAsInt("preyslimit") : 10;
				double preysCarriedSpeedReductionFactor = arguments
						.getArgumentIsDefined("preysCarriedSpeedReductionFactor") ? arguments
						.getArgumentAsDouble("preysCarriedSpeedReductionFactor")
						: 0.8;
				int penalizationDueToColision = arguments
						.getArgumentIsDefined("penalizationDueToColision") ? arguments
						.getArgumentAsInt("penalizationDueToColision") : 10;

				int amountOfSafeTime = arguments
						.getArgumentIsDefined("amountOfSafeTime") ? arguments
						.getArgumentAsInt("amountOfSafeTime") : 10;

				robot = new MultiPreyForagerRobot(simulator, robotName
						+ numberOfRobots++, position.x, position.y,
						orientation, mass, radius, distanceWheels,color, limit,
						preysCarriedSpeedReductionFactor,
						penalizationDueToColision, amountOfSafeTime);
			}
			
//			robot.setExtraRadius(extraRadius);

		} else {
			throw new RuntimeException("Unknown robot type: " + robotName);
		}
		return robot;
	}

	public void addSensors(Robot robot, 
			Arguments arguments) {
		if (!arguments.getArgumentIsDefined("sensors"))
			return;

		Arguments sensors = new Arguments(
				arguments.getArgumentAsString("sensors"));

		for (int i = 0; i < sensors.getNumberOfArguments(); i++) {
			Sensor sensor = createSensor(i, robot, simulator,
					sensors.getArgumentAt(i),
					new Arguments(sensors.getValueAt(i)));
			robot.addSensor(sensor);
		}
	}

	public void addActuators(Robot robot, 
			Arguments arguments) {
		if (!arguments.getArgumentIsDefined("actuators"))
			return;

		Arguments actuators = new Arguments(
				arguments.getArgumentAsString("actuators"));

		for (int i = 0; i < actuators.getNumberOfArguments(); i++) {
			Actuator actuator = createActuator(i, robot, 
					actuators.getArgumentAt(i),
					new Arguments(actuators.getValueAt(i)));
			robot.addActuator(actuator);
		}
	}

	public Sensor createSensor(int id, Robot robot, 
			Simulator simulator, String name, Arguments arguments) {
		if (arguments.getArgumentIsDefined("id")) {
			id = arguments.getArgumentAsInt("id");
		}
		if (name.equalsIgnoreCase("nestsensor")) {
			return new NestSensor(simulator,id,robot,arguments);
		} else if (name.equalsIgnoreCase("nearrobot") || name.equalsIgnoreCase("nearrobotsensor")) {
			return new NearRobotSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("lightsensor")) {
			return new LightTypeSensor(simulator, id, robot,arguments);
		} else if (name.equalsIgnoreCase("epucklightsensor")) {
			return new EpuckLightSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("PheromoneSensor")) {
			return new PheromoneSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("preysensor")) {
			return new PreySensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simplenestsensor")) {
			return new SimpleNestSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simpleteamnestsensor")) {
			return new SimpleLightTypeSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simplenestsensorperimeter")) {
			return new PerimeterSimpleLightTypeSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simplepreysensor")) {
			return new SimplePreySensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("XRayPreySensor")) {
			return new XRayPreySensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simplepreysensorperimeter")) {
			return new SimplePreySensorPerimeter(simulator, id, robot,arguments);
		} else if (name.equalsIgnoreCase("doubleparametersensor")) {
			return new DoubleParameterSensor(simulator, id, robot,arguments);
		} else if (name.equalsIgnoreCase("multypreycarriedsensor")) {
			return new MultyPreyCarriedSensor(simulator, id, robot,arguments);
		} else if (name.equalsIgnoreCase("preycarriedsensor")) {
			return new PreyCarriedSensor(simulator, id, robot,arguments);
		} else if (name.equalsIgnoreCase("innestsensor")) {
			return new InNestSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("compasssensor")) {
			return new CompassSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("positionsensor")) {
			return new PositionSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("energysensor")) {
			return new EnergySensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("robotcolorsensor")) {
			return new RobotColorSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simplerobotcolorsensor")) {
			return new SimpleRobotColorSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("simplerobotcolorsensorperimeter")) {
			return new PerimeterSimpleRobotColorSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("robotrgbcolorsensor")) {
			return new RobotRGBColorSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("groundrgbcolorsensor")) {
			return new GroundRGBColorSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("behaviorsensor")) {
			return new BehaviorSensor(simulator, id, robot, arguments);
		} else if (name.equalsIgnoreCase("epuckirsensor")) {
			return new EpuckIRSensor(simulator, id, robot,arguments);
		} else if (name.equalsIgnoreCase("wallbuttonsensor")) {
			return new WallButtonSensor(simulator, id, robot, arguments);
		} else {
			throw new RuntimeException("Unknown sensor: " + name);
		}
	}

	public Actuator createActuator(int id, Robot robot, 
			String name, Arguments arguments) {
		if (arguments.getArgumentIsDefined("id")) {
			id = arguments.getArgumentAsInt("id");
		}
		if (name.equalsIgnoreCase("twowheelact")
				|| name.equalsIgnoreCase("twowheel")
				|| name.equalsIgnoreCase("twowheelactuator")) {
			return new TwoWheelActuator(simulator, id, arguments);

		} else if (name.equalsIgnoreCase("color")
				|| name.equalsIgnoreCase("coloractuator")) {
			return new RobotColorActuator(simulator, id);
		} else if (name.equalsIgnoreCase("rgbcolor")
				|| name.equalsIgnoreCase("rgbcoloractuator")
				|| name.equalsIgnoreCase("robotrgbcolor")
				|| name.equalsIgnoreCase("robotrgbcoloractuator")) {
			return new RobotRGBColorActuator(simulator, id);
		} else if (name.equalsIgnoreCase("preypicker")
				|| name.equalsIgnoreCase("preypickeractuator")) {
			return new PreyPickerActuator(simulator, id, arguments);
		} else if (name.equalsIgnoreCase("SimplePreyPickerActuator")) {
			return new SimplePreyPickerActuator(simulator, id, arguments);
		} else if (name.equalsIgnoreCase("multipreypicker")
				|| name.equalsIgnoreCase("multipreypickeractuator")) {
			return new MultiPreyPickerActuator(simulator, id, arguments);
		} else if (name.equalsIgnoreCase("opendoor")
				|| name.equalsIgnoreCase("opendooractuator")) {
			return new OpenDoorActuator(simulator, id,arguments);
		}
		if (name.equalsIgnoreCase("behavior")
				|| name.equalsIgnoreCase("behavioractuator")) {
			BehaviorActuator b = new BehaviorActuator(simulator, id, robot,
					arguments);
			return b;
		}

		else
			throw new RuntimeException("Unknown actuator: " + name);
	}
}
