package roommaze;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ClutteredMazeEvaluationFunction extends TMazeEvaluationFunction {
	
	protected Robot r;
	protected ClutteredMazeEnvironment clutteredEnv;
	protected Vector2d startPosition = new Vector2d();
	protected boolean finishedRoom = false;
	private boolean onlyRoom = false;
	private double closestDistance = 0;

	public ClutteredMazeEvaluationFunction(Arguments args) {
		super(args);
		startPosition.x = 0;
		startPosition.y = 0;
		onlyRoom = args.getArgumentIsDefined("onlyroom");
	}
	
	public boolean reachedRoomFinish() {
		return getDistanceToRoomFinish() > 0.95;
	}

	@Override
	public void update(Simulator simulator) {
		if(env == null)
			env = (TMazeEnvironment)simulator.getEnvironment();
		if(clutteredEnv == null)
			clutteredEnv = (ClutteredMazeEnvironment)simulator.getEnvironment();
		if(r == null)
			r = simulator.getEnvironment().getRobots().get(0);
		
		if(!finishedRoom && reachedRoomFinish()) {
			finishedRoom = true;
			if(onlyRoom)
				simulator.stopSimulation();
		} else if(r.isInvolvedInCollison()) {
			touchedWall = true;
			simulator.stopSimulation();
		} else if(finishedRoom) {
			checkSquares();
			if(inFinalSquare || inForbiddenSquare)
				simulator.stopSimulation();
		}
		
		this.fitness = computeFitness(simulator);
	}
	
	private double computeFitness(Simulator simulator) {
		
		double fitness = getDistanceToRoomFinish();
		
		if(fitness > 0.95)
			fitness = 1;
		
		if(fitness > closestDistance)
			closestDistance = fitness;
		
		fitness = closestDistance;
		
		if(finishedRoom) {
			
			fitness = (5.0 + (maxSteps-simulator.getTime())/maxSteps)*fitness;
			if(closestDistance < fitness)
				closestDistance = fitness;
			
			fitness = closestDistance;
			
			if(inFinalSquare)
				fitness+= 1 + (maxSteps-simulator.getTime())/maxSteps;
			
			if(touchedWall || inForbiddenSquare)
				fitness+= wallCollisionFitness();
			
		} else {
			if(r.isInvolvedInCollison())
				fitness*=0.5;			
		}
		
		return fitness;
	}

	public boolean inRoom() {
		
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(clutteredEnv.getRoomExitPosition());
		exit.y+=clutteredEnv.getExitWidth();
		
		return robot.y < exit.y;
	}
	
	public double getDistanceToRoomFinish() {
		
		Vector2d start = startPosition;
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(clutteredEnv.getRoomExitPosition());
		exit.y+=clutteredEnv.getExitWidth();
		
		return (start.distanceTo(exit)-robot.distanceTo(exit))/start.distanceTo(exit);
	}
}