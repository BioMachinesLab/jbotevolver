package controllers;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuators.StateActuator;

public class StateBehaviorController extends BehaviorController {
	
	private StateActuator actuator;

	public StateBehaviorController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		Actuator a = robot.getActuatorByType(StateActuator.class);
		if(a == null)
			throw new RuntimeException("StateBehaviorController needs 'StateActuator'!");
		actuator = (StateActuator)a;
	}
	
	@Override
	public void controlStep(double time) {
		super.controlStep(time);
		for(int i = 0 ; i < actuator.getNumberOfStates() ; i++)
			actuator.setState(i, i != currentSubNetwork ? 0 : 1);
	}
}
