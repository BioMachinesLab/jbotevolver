package evaluationfunctions;

import sensors.BoundarySensor;
import sensors.InsideBoundarySensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SpreadEvaluationFunction extends EvaluationFunction {
	
	private int steps = 0;
	
	public SpreadEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
		
		double averageVal = 0;
		double division = 0;
		int numberOutside = 0;
		
		for(Robot r : simulator.getRobots()) {
			
			double robotMax = 0;
			
			BoundarySensor bs = (BoundarySensor)r.getSensorByType(BoundarySensor.class);
			InsideBoundarySensor ibs = (InsideBoundarySensor)r.getSensorByType(InsideBoundarySensor.class);
			RobotSensor rs = (RobotSensor)r.getSensorByType(RobotSensor.class);
			
			for(int i = 0 ; i < bs.getNumberOfSensors() ; i++) {
//				averageVal+=bs.getSensorReading(i);
				robotMax = Math.max(bs.getSensorReading(i), robotMax);
			}
			
			for(int i = 0 ; i < rs.getNumberOfSensors() ; i++) {
//				averageVal+=rs.getSensorReading(i);
				robotMax = Math.max(rs.getSensorReading(i), robotMax);
			}
			
//			division+= (rs.getNumberOfSensors()+bs.getNumberOfSensors());
			division+= 1;
			averageVal+=robotMax;//debug
			
			numberOutside+= 1-ibs.getSensorReading(0);
		}

		averageVal/=division;
		if(numberOutside == 0)
			fitness+= 1-averageVal;
	}
	
	@Override
	public double getFitness() {
		return super.getFitness()/(double)steps;
	}
}