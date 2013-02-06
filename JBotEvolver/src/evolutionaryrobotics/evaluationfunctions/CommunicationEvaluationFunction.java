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
	private int numberOfFoodForaged = 0;

	public CommunicationEvaluationFunction(Arguments args) {
		super(args);	
	}
	
	@Override
	public double getFitness() {
		return fitness + numberOfFoodForaged;
	}

	@Override
	public void update(Simulator simulator) {			
		
		double forbidenArea =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForbiddenArea();
		double foragingArea =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForageRadius();	
		
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
			
			this.numberOfFoodForaged = ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged();
			
			if(numberOfFoodForaged > 0) {
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