package experiments;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class RoomMazeBackExperiment extends Experiment{

	public RoomMazeBackExperiment(Simulator simulator, Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotArguments, Arguments controllerArguments) {
		super(simulator, experimentArguments, environmentArguments, robotArguments,
				controllerArguments);
		int additionalRobots = experimentArguments.getArgumentAsIntOrSetDefault("additionalrobots", 1);
		
		if(additionalRobots > 0)
			createAdditionalRobots(additionalRobots);
	}

	private void createAdditionalRobots(int additionalRobots) {
		
		Arguments controller = Arguments.createOrPrependArguments(null, "name=robotfollower");
		Arguments robot = Arguments.createOrPrependArguments(this.robotArguments, "+sensors=(epuckirsensor=(id=1,angle=60,numberofsensors=4),nearrobot=(id=2,range=0.1))");
		
		for(int i = 0 ; i < additionalRobots ; i ++) {
			Robot r = createOneRobot(robot, controller);
			environment.addRobot(r);
			r.setPosition(new Vector2d(3, 3));
		}
	}
}
