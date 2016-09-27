package controllers;

import java.util.LinkedList;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

public class ParameterController extends Controller {
	
	protected int numberOfBehaviors = 3;
	protected double parameterRange = 5;
	
	protected double parameter = 0;
	protected int behavior =  0;
	protected double behaviorPercentage = 0;
	
	protected LinkedList<Controller> subControllers;
	
	public ParameterController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		setParameter(args.getArgumentAsDoubleOrSetDefault("parameter", parameter));
		parameterRange = args.getArgumentAsDoubleOrSetDefault("parameterrange", parameterRange);
		numberOfBehaviors = args.getArgumentAsIntOrSetDefault("numberofbehaviors", numberOfBehaviors);
		
	}
	
	@Override
	public void controlStep(double time) {
		subControllers.get(behavior).controlStep(time);
	}
	
	private void updateBehavior() {
		double increment = parameterRange*2.0/numberOfBehaviors;
		double startVal = -parameterRange+increment;
		
		behavior = 0;
		behaviorPercentage = 0;
		
		double currentVal = 0;
		
		for(currentVal = startVal ; currentVal < parameterRange ; currentVal+=increment, behavior++) {
			if(parameter < currentVal) {
				break;
			}
		}
		behaviorPercentage = (parameter-currentVal+increment)/increment;
	}
	
	public double getParameter() {
		return parameter;
	}
	
	public void setParameter(double parameter) {
		this.parameter = Math.max(Math.min(parameterRange,parameter),-parameterRange);
		updateBehavior();
	}
	
	public void setSubControllers(LinkedList<Controller> subControllers) {
		this.subControllers = subControllers;
		
		if(subControllers != null) {
			numberOfBehaviors = subControllers.size();
		}
	}


}
