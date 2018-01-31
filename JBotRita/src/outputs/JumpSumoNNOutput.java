package outputs;


import actuator.JumpSumoActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpSumoNNOutput extends JumpNNOutput {

	public JumpSumoNNOutput(Actuator actuactor, Arguments args){
		super(actuactor, args);
		this.actuactor=( JumpSumoActuator) actuactor;
	}

}

