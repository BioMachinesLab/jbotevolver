package extensions;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MoveForwardEnvironment extends SimpleArenaEnvironment {

	protected boolean isHorizontalEnvironment, leftToRight, bottomToTop;
	protected final double SHORTEST_SIDE = 0.2, LONGEST_SIDE = 1.00;
	protected double initialX, initialY;
	
	public MoveForwardEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		Random random = simulator.getRandom();
		this.isHorizontalEnvironment = random.nextBoolean();
		if(!this.isHorizontalEnvironment){
			this.bottomToTop = random.nextBoolean();
			this.width = SHORTEST_SIDE;
			this.height = LONGEST_SIDE;
		}
		else {
			this.leftToRight = random.nextBoolean();
			this.height = SHORTEST_SIDE;
			this.width = LONGEST_SIDE;
		}
	}

	@Override
	public void update(double time) {
		
	}
	
	@Override
	public void setup(Simulator simulator){
		super.setup(simulator);
	}
	
	@Override
	protected void placeRobots(Simulator simulator) {
		Robot robot = this.getRobots().get(0);
		if(this.isHorizontalEnvironment){
			if(this.leftToRight){
				initialX  = -this.width/2;
				initialY = 0;
				robot.moveTo(new Vector2d(initialX, initialY));
				robot.setOrientation(Math.toRadians(0));
			}
			else {
				initialX  = this.width/2;
				initialY = 0;
				robot.moveTo(new Vector2d(initialX, initialY));
				robot.setOrientation(Math.toRadians(180));
			}
		}
		//vertically aligned environment.
		else {
			if(this.bottomToTop){
				initialX = 0;
				initialY = -this.height/2;
				robot.moveTo(new Vector2d(initialX, initialY));
				robot.setOrientation(90);
			}
			else {
				initialX = 0;
				initialY = this.height/2;
				robot.moveTo(new Vector2d(initialX, initialY));
				robot.setOrientation(Math.toRadians(270));
			}
		}
	}

	public double getTotalDistance(double range) {
		return this.LONGEST_SIDE - range;
	}

	public double computeTraveledDistance(Vector2d currentPosition) {
		//distance measured in a straight line.
		if(this.isHorizontalEnvironment)
			return Math.abs(this.initialX - currentPosition.getX());
		else
			return Math.abs(this.initialY - currentPosition.getY());
	}

}
