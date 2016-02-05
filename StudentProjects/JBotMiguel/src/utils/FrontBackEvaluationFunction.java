package utils;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import fourwheeledrobot.MultipleWheelRepertoireActuator;

public class FrontBackEvaluationFunction extends EvaluationFunction{
	
	protected double speed; 
	
	public FrontBackEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		
		double s = 0;
		
		Robot r = simulator.getRobots().get(0);
		MultipleWheelRepertoireActuator act = (MultipleWheelRepertoireActuator) r.getActuatorByType(MultipleWheelRepertoireActuator.class);
		for(double d : act.getCompleteSpeeds()) {
			s+=d;
		}
		s/=(double)act.getCompleteSpeeds().length;
		speed+=s;
	}
	
	@Override
	public double getFitness() {
		return speed;
	}

}
