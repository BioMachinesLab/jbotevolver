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

	public ControllerFactory(Simulator simulator) {
		super(simulator);
	}

	public Controller getController(Robot robot, Arguments arguments) {

		if (!arguments.getArgumentIsDefined("name"))
			throw new RuntimeException("Controller 'name' not defined: "+arguments.toString());

		String controllerName = arguments.getArgumentAsString("name");

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

	public Vector<NNInput> getNNInputs(Robot robot, Arguments arguments) {

		Arguments inputs = new Arguments(arguments.getArgumentAsString("inputs"));
		Vector<NNInput> nnInputs = new Vector<NNInput>();
		
		if (inputs.getNumberOfArguments() == 1 && inputs.getArgumentAt(0).equalsIgnoreCase("auto")) {
			nnInputs = getInputsAutomatically(robot);
		} else {
			for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
				NNInput nnInput = createInput(robot, inputs.getArgumentAt(i),new Arguments(inputs.getValueAt(i)));
				nnInputs.add(nnInput);
			}
		}
		return nnInputs;
	}

	private Vector<NNInput> getInputsAutomatically(Robot robot) {
		Vector<NNInput> nnInputs = new Vector<NNInput>();
		Iterator<Sensor> i = robot.getSensors().iterator();
		
		try {
			while (i.hasNext()) {
				Sensor sensor = i.next();
				String inputName = sensor.getClass().getSimpleName().replace("Sensor","NNInput");
				inputName = ClassSearchUtils.getClassFullName(inputName);
				
				Constructor<?>[] constructors = Class.forName(inputName).getDeclaredConstructors();
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

		Sensor sensor = robot.getSensorWithId(id);
		
		try {
			name = ClassSearchUtils.getClassFullName(name);
		
			if(name.endsWith("SysoutNNInput")) {
				Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 3 && params[0] == Simulator.class
							&& params[1] == Robot.class && params[2] == Arguments.class) {
						return (NNInput) constructor.newInstance(simulator, robot, arguments);
					}
				}
			} else {
				Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 1 && params[0] == Sensor.class) {
						return (NNInput) constructor.newInstance(sensor);
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		throw new RuntimeException("Unknown NNIinput: " + name);
	}

	public Vector<NNOutput> getNNOutputs(Robot robot, Arguments arguments) {
		Arguments outputs = new Arguments(arguments.getArgumentAsString("outputs"));
		
		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();

		if (outputs.getNumberOfArguments() == 1 && outputs.getArgumentAt(0).equalsIgnoreCase("auto"))
			nnOutputs = getOutputsAutomatically(robot);
		else {
			for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
				NNOutput nnOutput = createOutput(robot, outputs.getArgumentAt(i),new Arguments(outputs.getValueAt(i)));
				nnOutputs.add(nnOutput);
			}
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
							nnOutputs.add((NNOutput) constructor.newInstance(actuator));
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
		
		try {
			name = ClassSearchUtils.getClassFullName(name);
		
			if(name.endsWith("SysoutNNOutput") || name.endsWith("FixedNNOutput")) {
				Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 3 && params[0] == Simulator.class
							&& params[1] == Robot.class && params[2] == Arguments.class) {
						return (NNOutput) constructor.newInstance(simulator, robot, arguments);
					}
				}
			} else if(name.endsWith("SimpleNNOutput")) {
				Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 1 && params[1] == Arguments.class) {
						return (NNOutput) constructor.newInstance(arguments);
					}
				}
			} else {
				Actuator actuator = robot.getActuatorWithId(id);
				Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 1 && params[0] == Actuator.class) {
						return (NNOutput) constructor.newInstance(actuator);
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		throw new RuntimeException("Unknown NNOutput: " + name);
	}
}