package evolutionaryrobotics.neuralnetworks.outputs;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.Factory;

public abstract class NNOutput implements Serializable{
	public NNOutput(Actuator actuator, Arguments args){}
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
				String outputName = actuator.getClass().getSimpleName().replace("Actuator","NNOutput");
				nnOutputs.add((NNOutput)Factory.getInstance(outputName, actuator, new Arguments("")));
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
		
		if(name.endsWith("SysoutNNOutput") || name.endsWith("FixedNNOutput"))
			return (NNOutput)Factory.getInstance(arguments.getArgumentAsString("classname"),simulator, robot, arguments);
		else if(name.endsWith("SimpleNNOutput"))
			return (NNOutput)Factory.getInstance(arguments.getArgumentAsString("classname"),arguments);
		else {
			Actuator actuator = robot.getActuatorWithId(id);
			return (NNOutput)Factory.getInstance(arguments.getArgumentAsString("classname"),actuator,arguments);
		}
	}
}