package evaluationfunctions.flocking;

import java.util.ArrayList;

import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsWithCohesionWithinRange extends ReynoldsWithMovementAsDistanceFromCenter {
	
	public ReynoldsWithCohesionWithinRange(Arguments args) {
		super(args);
	}
	
	protected void cohesion(int i, ArrayList<Robot> robots , Robot robot){
		for(int j = i+1 ; j < robots.size() ; j++) {  
			if(robot.getPosition().distanceTo(robots.get(j).getPosition())<=cohensionDistance){
				cohension+=1;
			}
			numberOfRobotsForAvarage++;
		}
	}
		
}	