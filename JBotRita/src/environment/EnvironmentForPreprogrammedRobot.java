package environment;

import controllers.RandomRobotController;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class EnvironmentForPreprogrammedRobot extends EmptyEnviromentsWithFixPositions{
	
	public EnvironmentForPreprogrammedRobot(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		//Robot myRobot= new Robot()
		//argumenstclassname=simulation.robot.DifferentialDriveRobot,numberofrobots=5,sensors=(OrientationRobotsAverageSensor_1=(classname=sensors.OrientationRobotsAverageSensor,range=0.5,id=1),RobotSensor_2=(classname=simulation.robot.sensors.RobotSensor,range=0.5,numbersensors=4,id=2)),actuators=(TwoWheelActuator_1=(classname=simulation.robot.actuators.TwoWheelActuator,id=1))
		Arguments args=new Arguments("argumenstclassname=simulation.robot.DifferentialDriveRobot,numberofrobots=6,sensors=(OrientationRobotsAverageSensor_1=(classname=sensors.OrientationRobotsAverageSensor,range=0.5,id=1),RobotSensor_2=(classname=simulation.robot.sensors.RobotSensor,range=0.5,numbersensors=4,id=2)),actuators=(TwoWheelActuator_1=(classname=simulation.robot.actuators.TwoWheelActuator,id=1))");
		DifferentialDriveRobot myRobot= new DifferentialDriveRobot(simulator, args);
		myRobot.setController(new RandomRobotController( simulator,  myRobot, args));
		robots.add(myRobot);
		for(Robot r : robots) {
			double x = simulator.getRandom().nextDouble()*distance*2-distance;
			double y = simulator.getRandom().nextDouble()*distance*2-distance;
			r.setPosition(x, y);
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}

	}

}
