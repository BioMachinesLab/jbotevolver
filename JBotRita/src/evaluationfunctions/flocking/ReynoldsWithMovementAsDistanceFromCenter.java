package evaluationfunctions.flocking;

import mathutils.Vector2d;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsWithMovementAsDistanceFromCenter extends ReynoldsFlocking {
	
	private Vector2d center = new Vector2d(0, 0);
	
	public ReynoldsWithMovementAsDistanceFromCenter(Arguments args) {
		super(args);
	}
	
	@Override
	protected void movement(Robot r){
		double percentageOfDistanceToCenter=r.getPosition().distanceTo(center)/simulator.getEnvironment().getWidth();;
		movementContribution+= percentageOfDistanceToCenter>1? 1:percentageOfDistanceToCenter;
	}
	

	
}
