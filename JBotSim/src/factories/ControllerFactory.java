package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;
import controllers.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.EpuckLightSensor;
import simulation.robot.sensors.WallButtonSensor;
import simulation.robot.sensors.XRayPreySensor;
import simulation.robot.sensors.CompassSensor;
import simulation.robot.sensors.DoubleParameterSensor;
import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.GroundRGBColorSensor;
import simulation.robot.sensors.InNestSensor;
import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.NearRobotSensor;
import simulation.robot.sensors.PerimeterSimpleLightTypeSensor;
import simulation.robot.sensors.PerimeterSimpleRobotColorSensor;
import simulation.robot.sensors.PositionSensor;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.PreySensor;
import simulation.robot.sensors.RobotColorSensor;
import simulation.robot.sensors.RobotRGBColorSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.SimpleLightTypeSensor;
import simulation.robot.sensors.SimpleRobotColorSensor;
import simulation.util.Arguments;
import simulation.util.ClassSearchUtils;
import evolutionaryrobotics.neuralnetworks.inputs.CompassNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.DoubleParameterNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.EpuckIRNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.GroundRGBColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.InNestNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.LightTypeNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.inputs.NearRobotNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.PositionNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.PreyCarriedNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.RobotColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.RobotRGBColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.SimpleLightTypeNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.SimpleRobotColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;
import evolutionaryrobotics.neuralnetworks.outputs.FixedNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.MultiNeuronRobotColorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.OpenDoorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.PreyPickerNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.RobotColorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.RobotRGBColorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SimpleNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SysoutNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.TwoWheelNNOutput;

public class ControllerFactory extends Factory implements Serializable {

	public ControllerFactory(Simulator simulator) {
		super(simulator);
	}

	public Controller getController(Robot robot, Arguments arguments) {

		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("Controller 'name' not defined: "
					+ arguments.toString());
		}

		String controllerName = arguments.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(controllerName)
					.getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == Simulator.class
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

	public Vector<NNInput> getNNInputs(Robot robot, Arguments arguments) {

		Arguments inputs = new Arguments(
				arguments.getArgumentAsString("inputs"));

		if (inputs.getNumberOfArguments() == 1) {
			if (inputs.getArgumentAt(0).equalsIgnoreCase("auto")) {
				return getInputsAutomatically(robot);
			}
		}

		Vector<NNInput> nnInputs = new Vector<NNInput>();

		for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
			NNInput nnInput = createInput(robot, inputs.getArgumentAt(i),
					new Arguments(inputs.getValueAt(i)));
			nnInputs.add(nnInput);
		}
		return nnInputs;
	}

	protected Vector<NNInput> getInputsAutomatically(Robot robot) {
		Vector<NNInput> nnInputs = new Vector<NNInput>();
		Iterator<Sensor> i = robot.getSensors().iterator();
		
		try {
			while (i.hasNext()) {
				Sensor sensor = i.next();
				String inputName = sensor.getClass().getSimpleName().replace("Sensor","NNInput");
				inputName = ClassSearchUtils.getClassFullName(inputName);
				
					Constructor<?>[] constructors = Class.forName(inputName)
							.getDeclaredConstructors();
					for (Constructor<?> constructor : constructors) {
						Class<?>[] params = constructor.getParameterTypes();
						if (params.length == 1 && params[0] == Sensor.class) {
							nnInputs.add((NNInput) constructor.newInstance(sensor));
						}
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		return nnInputs;
	}

	public NNInput createInput(Robot robot, String name, Arguments arguments) {
		int id = 0;
		if (arguments.getArgumentIsDefined("id"))
			id = arguments.getArgumentAsInt("id");

		if (name.equalsIgnoreCase("lighttype")
				|| name.equalsIgnoreCase("lighttypeinput")
				|| name.equalsIgnoreCase("epucklight")
				|| name.equalsIgnoreCase("epucklightinput")
				|| name.equalsIgnoreCase("prey")
				|| name.equalsIgnoreCase("preyinput")
				|| name.equalsIgnoreCase("wallbutton")
				|| name.equalsIgnoreCase("wallbuttoninput")) {
			return new LightTypeNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("simplelighttype")
				|| name.equalsIgnoreCase("simplelighttypeinput")) {
			return new SimpleLightTypeNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("robotcolor")
				|| name.equalsIgnoreCase("robotcolorinput")) {
			return new RobotColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("robotrgbcolor")
				|| name.equalsIgnoreCase("robotrgbcolorinput")) {
			return new RobotRGBColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("simplerobotcolor")
				|| name.equalsIgnoreCase("simplerobotcolorinput")) {
			return new SimpleRobotColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("preycarried")
				|| name.equalsIgnoreCase("preycarriedinput")) {
			return new PreyCarriedNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("innest")
				|| name.equalsIgnoreCase("innestinput")) {
			return new InNestNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("nearrobot")
				|| name.equalsIgnoreCase("nearrobotinput")) {
			return new NearRobotNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("compass")) {
			return new CompassNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("position")) {
			return new PositionNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("doubleparameter")) {
			return new DoubleParameterNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("monitor")
				|| name.equalsIgnoreCase("monitorinput")) {
			String new_name = arguments.getArgumentAt(0);
			Arguments actuators = new Arguments(
					arguments.getArgumentAsString(new_name));
			return new SysoutNNInput(createInput(robot, new_name, actuators));
		} else if (name.equalsIgnoreCase("groundrgbcolor")
				|| name.equalsIgnoreCase("groundrgbcolorsensor")) {
			return new GroundRGBColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("epuckir")
				|| name.equalsIgnoreCase("epuckirsensor")) {
			return new EpuckIRNNInput(robot.getSensorWithId(id));
		} else
			throw new RuntimeException("Unknown nn input: " + name);
	}

	public Vector<NNOutput> getNNOutputs(Robot robot, Arguments arguments) {
		Arguments outputs = new Arguments(
				arguments.getArgumentAsString("outputs"));

		if (outputs.getNumberOfArguments() == 1) {
			if (outputs.getArgumentAt(0).equalsIgnoreCase("auto")) {
				return getOutputsAutomatically(robot);
			}
		}

		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();

		for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
			NNOutput nnOutput = createOutput(robot, outputs.getArgumentAt(i),
					new Arguments(outputs.getValueAt(i)));
			nnOutputs.add(nnOutput);
		}

		return nnOutputs;
	}

	protected Vector<NNOutput> getOutputsAutomatically(Robot robot) {
		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();
		Iterator<Actuator> i = robot.getActuators().iterator();

		try {
			while (i.hasNext()) {
				Actuator actuator = i.next();
				String inputName = actuator.getClass().getSimpleName().replace("Actuator","NNOutput");
				inputName = ClassSearchUtils.getClassFullName(inputName);
				
					Constructor<?>[] constructors = Class.forName(inputName)
							.getDeclaredConstructors();
					for (Constructor<?> constructor : constructors) {
						Class<?>[] params = constructor.getParameterTypes();
						if (params.length == 1 && params[0] == Actuator.class) {
							nnOutputs.add((NNOutput) constructor.newInstance(nnOutputs));
						}
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		return nnOutputs;
	}

	public NNOutput createOutput(Robot robot, String name, Arguments arguments) {
		int id = 0;
		if (arguments.getArgumentIsDefined("id"))
			id = arguments.getArgumentAsInt("id");

		if (name.equalsIgnoreCase("twowheel")
				|| name.equalsIgnoreCase("twowheeloutput")) {
			return new TwoWheelNNOutput(robot.getActuatorWithId(id));
		} else if (name.equalsIgnoreCase("robotcolor")
				|| name.equalsIgnoreCase("robotcoloroutput")) {
			return new RobotColorNNOutput(robot.getActuatorWithId(id),
					arguments);
		} else if (name.equalsIgnoreCase("robotrgbcolor")
				|| name.equalsIgnoreCase("robotrgbcoloroutput")) {
			return new RobotRGBColorNNOutput(robot.getActuatorWithId(id),
					arguments);
		} else if (name.equalsIgnoreCase("multineuroncolor")
				|| name.equalsIgnoreCase("multineuroncoloroutput")) {
			return new MultiNeuronRobotColorNNOutput(
					robot.getActuatorWithId(id), arguments);
		} else if (name.equalsIgnoreCase("preypicker")
				|| name.equalsIgnoreCase("preypickeroutput")) {
			return new PreyPickerNNOutput(robot.getActuatorWithId(id),
					arguments);
		} else if (name.equalsIgnoreCase("monitor")
				|| name.equalsIgnoreCase("monitoroutput")) {
			// TODO
			String new_name = arguments.getArgumentAt(0);
			Arguments actuators = new Arguments(
					arguments.getArgumentAsString(new_name));
			return new SysoutNNOutput(createOutput(robot, new_name, actuators));
		} else if (name.equalsIgnoreCase("fixed")
				|| name.equalsIgnoreCase("fixedoutput")) {
			// TODO
			String new_name = arguments.getArgumentAt(0);
			Arguments actuators = new Arguments(
					arguments.getArgumentAsString(new_name));
			return new FixedNNOutput(createOutput(robot, new_name, actuators),
					arguments.getArgumentAsDouble("value"));
		} else if (name.equalsIgnoreCase("simple")
				|| name.equalsIgnoreCase("simpleoutput")) {
			// TODO
			return new SimpleNNOutput(
					arguments.getArgumentAsInt("numberofoutputs"));
		} else if (name.equalsIgnoreCase("opendooroutput")
				|| name.equalsIgnoreCase("opendoor")) {
			return new OpenDoorNNOutput(robot.getActuatorWithId(id), arguments);
		} else
			throw new RuntimeException("Unknown nn output: " + name);
	}
}