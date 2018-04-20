package sensors;

import java.util.ArrayList;
import java.util.HashMap;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;

public class RobotSensorWithSources extends RobotSensor {
	
	protected PhysicalObject[] sources;

	public RobotSensorWithSources(Simulator simulator, int id, Robot robot,Arguments args) {
		super(simulator, id, robot, args);
		sources=new PhysicalObject [numberOfSensors];
	}
	
	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++) {
			
			double sourceContribition=calculateContributionToSensor(j, source);
			
			if(sourceContribition>readings[j]){
				readings[j]=sourceContribition;
				sources[j]=source.getObject();
			}
			//readings[j] = Math.max(calculateContributionToSensor(j, source), readings[j]);
		}
	}
	
	
	public PhysicalObject [] getSourcesPerceived(){
		return sources;
	}
	
}
