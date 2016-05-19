package evaluationfunctions;

import sensors.BoundarySensor;
import sensors.InsideBoundarySensor;
import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class FollowIntruderEvaluationFunction extends EvaluationFunction{
	
	public FollowIntruderEvaluationFunction(Arguments args) {
		super(args);
	}

	private double steps = 0;

	@Override
	public void update(Simulator simulator) {
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
		
//		double averageVal = 0;
		double division = 0;
		
		int numberDetecting = 0;
		
		int numberOutside = 0;
		
		for(Robot r : simulator.getRobots()) {
			
			if(r.isEnabled() && !r.getDescription().equals("prey")) {
			
//				double robotMax = 0;
				
//				BoundarySensor bs = (BoundarySensor)r.getSensorByType(BoundarySensor.class);
				InsideBoundarySensor ibs = (InsideBoundarySensor)r.getSensorByType(InsideBoundarySensor.class);
				IntruderSensor is = (IntruderSensor)r.getSensorByType(IntruderSensor.class);
				
				if(is.foundIntruder())
					numberDetecting++;
				
//				for(int i = 0 ; i < bs.getNumberOfSensors() ; i++) {
//					robotMax = Math.max(bs.getSensorReading(i), robotMax);
//				}
				
				division+= 1;
//				averageVal+=robotMax;
				
				numberOutside+= 1-ibs.getSensorReading(0);
			
			}
		}
		
//		averageVal/=division;
		if(numberOutside == 0) {
			fitness+=numberDetecting;
			fitness-=numberOutside*10;
//			fitness+= (1-averageVal);
		}
	}
	
	@Override
	public double getFitness() {
		return super.getFitness()/(double)steps;
	}
}