package evaluationfunctions;

import java.util.ArrayList;

import sensors.RobotOrientationSensor;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class FlockEvaluationFunction extends EvaluationFunction {

	public FlockEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		
		double currentFitness = 0;
		double maxDistance = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			if(robots.get(i).isInvolvedInCollison()) {
				currentFitness-=1;
				continue;
			}
			
			for(int j = i+1 ; j < robots.size() ; j++) {
				double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
				
				if(distance > maxDistance)
					maxDistance = distance;
				
				if(distance < 1)
					currentFitness+= 1-distance;
				
				//maybe check their orientation here
				if(Math.abs(angle(robots.get(i).getOrientation(),robots.get(j).getOrientation())) < 1) //in radians
					currentFitness+=1;
			}
			
			DifferentialDriveRobot r = ((DifferentialDriveRobot)robots.get(i));

			currentFitness+=r.getLeftWheelSpeed()+r.getRightWheelSpeed();
			
			//penalize turns
			if(Math.abs(r.getLeftWheelSpeed()-r.getRightWheelSpeed()) > 0.02 || r.getLeftWheelSpeed() < 0.08|| maxDistance > 0.8)
				currentFitness = 0;
			
		}
		fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
	}
	
	public double angle(double a, double b) {
		double i = b-a;
		
		if(Math.abs(i) > Math.PI) {
			if(i > 0)
				i=-(2*Math.PI-Math.abs(i));
			else
				i = (2*Math.PI-Math.abs(i));
		}
		return i;
	}
}