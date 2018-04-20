package evaluationfunctions.flocking;

import mathutils.Vector2d;

import sensors.RobotSensorWithSources;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsTrulyPriority extends ReynoldsTruly {
	
	public ReynoldsTrulyPriority(Arguments args) {
		super(args);	
	}
	
	@Override
	protected void cohesion(int i, Robot robot){
		
		Vector2d robotPosition=robots.get(i).getPosition();
		PhysicalObject [] neighboursPerceived=(((RobotSensorWithSources)robots.get(i).getSensorByType(RobotSensorWithSources.class)).getSourcesPerceived());;

		double centerOfMassX=0, centerOfMassY=0,numberOfNeighboursPerceived=0;
		
		for(int j = 0 ; j < robots.size() ; j++) {  
			
			if(contains(neighboursPerceived,robots.get(j))){
				Vector2d neighbourPosition=robots.get(j).getPosition();
				centerOfMassX+=neighbourPosition.x;
				centerOfMassY+=neighbourPosition.y;
				numberOfNeighboursPerceived++;
			}
			
		}
		
		Vector2d centerOfMass=new Vector2d(centerOfMassX/numberOfNeighboursPerceived, centerOfMassY/numberOfNeighboursPerceived);
		double distanceToCenterMass=robotPosition.distanceTo(centerOfMass);
		
		if(distanceToCenterMass>=0.15){ //only scores cohesion, if they are within a given distance
			currentCohesion+=1-distanceToCenterMass/cohensionDistance;	
		}		
	}

}
