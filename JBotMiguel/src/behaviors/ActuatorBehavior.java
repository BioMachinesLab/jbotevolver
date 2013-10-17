package behaviors;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.behaviors.Behavior;
import simulation.robot.behaviors.OpenDoorBehavior;
import simulation.util.Arguments;

public class ActuatorBehavior extends Behavior{
	
	private NNOutput output;
	
	public ActuatorBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		
		Arguments outputArgs = new Arguments(args.getArgumentAsString("output"));
		
//		String name = outputArgs.getArgumentAsString("classname");
		
		output = NNOutput.createOutput(simulator, r, outputArgs.getArgumentAsString("classname"), outputArgs);
	}

	@Override
	public void controlStep(double time, double value) {
		output.setValue(0, value);
		output.apply();
	}

	@Override
	public void controlStep(double time) {
		// TODO Auto-generated method stub
		
	}
}