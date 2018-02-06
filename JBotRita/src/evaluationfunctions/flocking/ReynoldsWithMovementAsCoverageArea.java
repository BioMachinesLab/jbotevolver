package evaluationfunctions.flocking;

import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ReynoldsWithMovementAsCoverageArea extends ReynoldsFlocking {
	
	LinkedList<Integer> positionsOccupied = new LinkedList<Integer>();
	
	@Override
	public double getFitness() {
		Environment env=simulator.getEnvironment();
		fitnessForMovement=positionsOccupied.size()/(double)(env.getHeight()*env.getWidth());
		return super.getFitness();
	}
	
	public ReynoldsWithMovementAsCoverageArea(Arguments args) {
		super(args);
	}
	
	@Override
	protected void movement(Robot r){
			Vector2d robot_pos=r.getPosition();
			int posOfGrid=((int) robot_pos.x)*10 + (int) robot_pos.y;
			if(!positionsOccupied.contains(posOfGrid)){
				positionsOccupied.add(posOfGrid);
			}
	}	
	
	
}
