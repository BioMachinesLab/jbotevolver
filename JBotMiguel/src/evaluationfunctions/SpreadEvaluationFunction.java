package evaluationfunctions;

import sensors.BoundarySensor;
import sensors.InsideBoundarySensor;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SpreadEvaluationFunction extends EvaluationFunction {
	
	private int steps = 0;
	private boolean enableSpeed = false;
	
	public SpreadEvaluationFunction(Arguments args) {
		super(args);
		enableSpeed = args.getArgumentAsIntOrSetDefault("speed", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(steps == 0)
			steps = simulator.getEnvironment().getSteps();
		
		double averageVal = 0;
		double division = 0;
		int numberOutside = 0;
		
		double speed = 0;
		
		for(Robot r : simulator.getRobots()) {
			
			if(r.isEnabled() && !r.getDescription().equals("prey")) {
			
				double robotMax = 0;
				
				BoundarySensor bs = (BoundarySensor)r.getSensorByType(BoundarySensor.class);
				InsideBoundarySensor ibs = (InsideBoundarySensor)r.getSensorByType(InsideBoundarySensor.class);
				RobotSensor rs = (RobotSensor)r.getSensorByType(RobotSensor.class);
				
				for(int i = 0 ; i < bs.getNumberOfSensors() ; i++) {
					robotMax = Math.max(bs.getSensorReading(i), robotMax);
				}
				
				for(int i = 0 ; i < rs.getNumberOfSensors() ; i++) {
					robotMax = Math.max(rs.getSensorReading(i), robotMax);
				}
				double l = Math.abs(((DifferentialDriveRobot)r).getLeftWheelSpeed());
				double rr = Math.abs(((DifferentialDriveRobot)r).getRightWheelSpeed());
				speed+=(l+rr)/2;
				
				division+= 1;
				averageVal+=robotMax;
				numberOutside+= 1-ibs.getSensorReading(0);
			}
		}
		speed/=division;
		double maxSpeed = 2.77;

		speed = (maxSpeed-speed)/maxSpeed;
		averageVal/=division;
		if(numberOutside == 0) {
			if(!enableSpeed)
				fitness+= (1-averageVal);
			else
				fitness+= (1-averageVal)*0.66+speed*0.33;
		}
	}
	
	@Override
	public double getFitness() {
		return super.getFitness()/(double)steps;
	}
}