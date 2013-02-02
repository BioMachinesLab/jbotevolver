package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.GroupedPreyEnvironment;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class CommunicationEvaluationFunction extends EvaluationFunction{
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double		forbidenArea;
	private double		foragingArea;

	public CommunicationEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator,args);
		forbidenArea =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForbiddenArea();
		foragingArea =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForageRadius();		
	}
	
	@Override
	public double getFitness() {
		return fitness + ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() * 1.0;
	}

	private boolean haveForaged(){
		return ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() > 0;
	}

	@Override
	public void update(double time) {			
		int numberOfRobotsWithPrey       = 0;
		int numberOfRobotsBeyondForbidenLimit       = 0;
		int numberOfRobotsBeyondForagingLimit       = 0;
		
		double collectiveSpeed = 0;

		for(Robot r : simulator.getEnvironment().getRobots()){
			double distanceToNest = r.getPosition().distanceTo(nestPosition);
		
			if(distanceToNest > forbidenArea){
				numberOfRobotsBeyondForbidenLimit++;
			} else 	if(distanceToNest > foragingArea){
				numberOfRobotsBeyondForagingLimit++;
			}
			
			if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()) {
				numberOfRobotsWithPrey++;
			}
			
			if(haveForaged()) {
				DifferentialDriveRobot ddr = (DifferentialDriveRobot)r;
				collectiveSpeed+= (Math.abs(ddr.getLeftWheelSpeed())+Math.abs(ddr.getRightWheelSpeed()))/2;
			}
		}

		fitness += (double) numberOfRobotsWithPrey * 0.001 + 
			numberOfRobotsBeyondForbidenLimit * -0.1 + 
			numberOfRobotsBeyondForagingLimit * -0.0001 +
			collectiveSpeed * -0.0001;
	}
}