package arbitrators;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.PreySensor;
import simulation.robot.sensors.RobotRGBColorSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class CrossForageArbitrator extends PreprogrammedArbitrator {
	
	private PreySensor preySensor;
	private RobotRGBColorSensor colorSensor;
	private WallRaySensor wallSensor;
	private boolean foraging = false;
	private int currentSteps = 0;
	private int thresholdSteps = 500;

	public CrossForageArbitrator(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		
		if(subControllers.get("forage") == null)
			throw new RuntimeException("CrossForageArbitrator needs 'forage' subcontroller");
		if(subControllers.get("open_cross") == null)
			throw new RuntimeException("CrossForageArbitrator needs 'open_cross' subcontroller");
		if(subControllers.get("_color") == null)
			throw new RuntimeException("CrossForageArbitrator needs '_color' subcontroller");
		
		Sensor s = robot.getSensorByType(PreySensor.class);
		if(s == null)
			throw new RuntimeException("CrossForageArbitrator needs 'PreySensor' sensor");
		preySensor = (PreySensor)s;
		s.setEnabled(true);
		
		s = robot.getSensorByType(RobotRGBColorSensor.class);
		if(s == null)
			throw new RuntimeException("CrossForageArbitrator needs 'RobotRGBColorSensor' sensor");
		colorSensor = (RobotRGBColorSensor)s;
		s.setEnabled(true);
		
		s = robot.getSensorByType(WallRaySensor.class);
		if(s == null)
			throw new RuntimeException("CrossForageArbitrator needs 'WallRaySensor' sensor");
		wallSensor = (WallRaySensor)s;
		s.setEnabled(true);
	}
	
	@Override
	public void controlStep(double time) {
		double maxVal = 0;
		currentSteps++;
		for(int i = 0 ; i < preySensor.getNumberOfSensors() ; i++) {
			double currentVal = preySensor.getSensorReading(i);
			maxVal = maxVal > currentVal ? maxVal : currentVal;
		}
		
		for(int i = colorSensor.slices ; i < colorSensor.getNumberOfSensors() ; i++) {
			double currentVal = colorSensor.getSensorReading(i);
			maxVal = maxVal > currentVal ? maxVal : currentVal;
		}
		
		double right = wallSensor.getSensorReading(6);
		
//		if(right > 0.2 && !foraging){
//			maxVal = 0;
//		}
		
		if(maxVal > 0) {
			if(!foraging) {
				foraging = true;
				subControllers.get("forage").reset();
				currentSteps = 0;
			}
			subControllers.get("forage").controlStep(time);
		} else {
			if(foraging) {
				foraging = false;
				subControllers.get("open_cross").reset();
				currentSteps = 0;
			}
//			if(currentSteps > thresholdSteps)
//				foraging = true;
			subControllers.get("open_cross").controlStep(time);
		}
		
		subControllers.get("_color").controlStep(time);
	}
}