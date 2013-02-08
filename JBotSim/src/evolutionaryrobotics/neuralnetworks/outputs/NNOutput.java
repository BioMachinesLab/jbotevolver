package evolutionaryrobotics.neuralnetworks.outputs;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ClassSearchUtils;

public abstract class NNOutput implements Serializable{
	public abstract int getNumberOfOutputValues();
	public abstract void setValue(int index, double value);
	public abstract void apply();
	
	public static Vector<NNOutput> getNNOutputs(Simulator simulator, Robot robot, Arguments arguments) {
		Arguments outputs = new Arguments(arguments.getArgumentAsString("outputs"));
		
		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();

		if (outputs.getNumberOfArguments() == 1 && outputs.getArgumentAt(0).equalsIgnoreCase("auto"))
			nnOutputs = getOutputsAutomatically(robot);
		else {
			for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
				NNOutput nnOutput = createOutput(simulator, robot, outputs.getArgumentAt(i),new Arguments(outputs.getValueAt(i)));
				nnOutputs.add(nnOutput);
			}
		}
		return nnOutputs;
	}

	protected static Vector<NNOutput> getOutputsAutomatically(Robot robot) {
		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();
		Iterator<Actuator> i = robot.getActuators().iterator();

		try {
			while (i.hasNext()) {
				Actuator actuator = i.next();
				String inputName = actuator.getClass().getSimpleName().replace("Actuator","NNOutput");
				
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

	public static NNOutput createOutput(Simulator simulator, Robot robot, String name, Arguments arguments) {
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