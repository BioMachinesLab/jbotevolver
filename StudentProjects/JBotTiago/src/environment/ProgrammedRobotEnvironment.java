package environment;

import mathutils.Vector2d;
import sensors.ThymioIRSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.StayStoppedController;

public class ProgrammedRobotEnvironment extends Environment {
	private double forageLimit;
	private double forbiddenArea;
	
	
	public ProgrammedRobotEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		Arguments programmedRobotArguments = simulator.getArguments().get("--robots");
		Robot robot = Robot.getRobot(simulator, programmedRobotArguments);
		robot.setController(new StayStoppedController(simulator, robot, programmedRobotArguments));
		robot.setPosition(new Vector2d(-0.15, 0.04));
//		robot.setPosition(new Vector2d(-0.15, -0.020));
//		robot.setPosition(new Vector2d(-0.15, -0.07));
		addRobot(robot);
		
	}
	
	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	
	@Override
	public void update(double time) {
		for (Robot r : robots) {
			if(r.getId() == 0){
				ThymioIRSensor irSensors = (ThymioIRSensor) r.getSensorByType(ThymioIRSensor.class);
				for (int i = 0; i < irSensors.getNumberOfSensors(); i++) 
					System.out.println("Sensors " + i + ": " + irSensors.getSensorReading(i));
			}
		}
		System.out.println(" -------- ");
	}
	
}