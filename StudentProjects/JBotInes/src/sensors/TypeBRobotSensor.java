package sensors;

import java.util.ArrayList;

import robots.AltDifferentialDriveRobot;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;
import checkers.AllowTypeBRobotsChecker;

public class TypeBRobotSensor extends LightTypeSensor{

	public TypeBRobotSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowTypeBRobotsChecker(robot.getId()));
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {

		if(closeObjects != null)
			closeObjects.update(time, teleported);
		//		if(robot.getId()==0)
		//			System.out.println(closeObjects.getNumberOfCloseObjects()+" "+time);
		try { 
			for(int j = 0; j < readings.length; j++){
				readings[j] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if(source.getObject().isInvisible())
					continue;
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
					iterator.updateCurrentDistance(this.geoCalc.getDistanceBetween(sensorPosition, source.getObject(), time));
				}
			}

			if(checkObstacles)
				checkObstacles(time, teleported);

		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {

		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);

		if((sensorInfo.getDistance() < getCutOff()) && 
				(sensorInfo.getAngle() < (openingAngle / 2.0)) && 
				(sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			if(source.getObject() instanceof AltDifferentialDriveRobot) {
				AltDifferentialDriveRobot robot = (AltDifferentialDriveRobot) source.getObject();
				int numbNeighbors = robot.getNeighbors().size();

				return ((2/(1 + Math.exp(-numbNeighbors/3.0)))-1); // y=2/1+exp(-x/3) -1 

			}
		}

		return 0;
	}

}