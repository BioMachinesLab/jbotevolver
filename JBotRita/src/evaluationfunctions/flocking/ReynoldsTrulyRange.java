package evaluationfunctions.flocking;

import mathutils.Vector2d;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsTrulyRange extends ReynoldsTruly {
	
	public ReynoldsTrulyRange(Arguments args) {
		super(args);	
	}
	
	@Override
	protected void cohesion(int i, Robot robot){
		
		Vector2d robotPosition=robot.getPosition();
		double centerOfMassX=0, centerOfMassY=0,numberOfNeighboursInRange=0;
		for(int j = 0 ; j < robots.size() ; j++) {  
			Vector2d neighbourPosition=robots.get(j).getPosition();
			if(robotPosition.distanceTo(neighbourPosition)<=cohensionDistance){
				if ( i!= j) {
					centerOfMassX+=neighbourPosition.x;
					centerOfMassY+=neighbourPosition.y;
					numberOfNeighboursInRange++;
				}
			}
		}
		Vector2d centerOfMass=new Vector2d(centerOfMassX/numberOfNeighboursInRange, centerOfMassY/numberOfNeighboursInRange);
		double distanceToCenterMass=robotPosition.distanceTo(centerOfMass);
		if(numberOfNeighboursInRange>=1){ //only scores cohesion, if they are within a given distance
			currentCohesion+=1-distanceToCenterMass/cohensionDistance;	
		}	
	}
	
}