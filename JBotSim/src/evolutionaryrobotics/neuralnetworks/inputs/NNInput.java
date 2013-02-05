package evolutionaryrobotics.neuralnetworks.inputs;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ClassSearchUtils;

public abstract class NNInput implements Serializable {
	public abstract int getNumberOfInputValues();
	public abstract double getValue(int index);
	
	public static Vector<NNInput> getNNInputs(Simulator simulator, Robot robot, Arguments arguments) {

		Arguments inputs = new Arguments(arguments.getArgumentAsString("inputs"));
		Vector<NNInput> nnInputs = new Vector<NNInput>();
		
		if (inputs.getNumberOfArguments() == 1 && inputs.getArgumentAt(0).equalsIgnoreCase("auto")) {
			nnInputs = getInputsAutomatically(robot);
		} else {
			for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
				NNInput nnInput = createInput(simulator, robot, inputs.getArgumentAt(i),new Arguments(inputs.getValueAt(i)));
				nnInputs.add(nnInput);
			}
		}
		return nnInputs;
	}

	private static Vector<NNInput> getInputsAutomatically(Robot robot) {
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

	public static NNInput createInput(Simulator simulator, Robot robot, String name, Arguments arguments) {
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
}
