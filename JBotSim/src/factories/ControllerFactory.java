package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;
import controllers.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ClassSearchUtils;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class ControllerFactory extends Factory implements Serializable {

	public static Controller getController(Simulator simulator, Robot robot, Arguments arguments) {

		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Controller 'name' not defined: "+arguments.toString());

		String controllerName = arguments.getArgumentAsString("classname");
		
		try {
			Constructor<?>[] constructors = Class.forName(controllerName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 3 && params[0] == Simulator.class
						&& params[1] == Robot.class && params[2] == Arguments.class) {
					return (Controller) constructor.newInstance(simulator,robot,arguments);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		throw new RuntimeException("Unknown controller: " + controllerName);
	}
}