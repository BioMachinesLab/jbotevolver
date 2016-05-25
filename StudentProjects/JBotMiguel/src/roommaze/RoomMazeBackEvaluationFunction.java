package roommaze;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.util.Arguments;

public class RoomMazeBackEvaluationFunction extends ClutteredMazeEvaluationFunction {
	
	private boolean finishedRoom = false;
	private boolean finishedMaze = false;
	private boolean wentBack = false;
	private double mazePenalty = 0;

	public RoomMazeBackEvaluationFunction(Arguments args) {
		super(args);
		mazePenalty = args.getArgumentAsDoubleOrSetDefault("mazepenalty", 0);
	}

	@Override
	public void update(Simulator simulator) {
		
		if(r.isInvolvedInCollison())
			simulator.stopSimulation();
		
		if(!finishedRoom) { //Didn't clear the Cluttered Room
			stepUnfinishedRoom(simulator);
		} else if(!finishedMaze) { //Cleared the Cluttered Room
			stepFinishedRoom(simulator);
		} else if(!wentBack) { //Cleared the Maze
			stepFinishedMaze(simulator);
		} else { //Cleared everything
			simulator.stopSimulation();
		}
		
		this.fitness = computeFitness();
	}
	
	private void stepUnfinishedRoom(Simulator simulator) {
		if(!inRoom())
			finishedRoom = true;
	}
	
	private void stepFinishedRoom(Simulator simulator) {
		checkSquares();

		finishedMaze = inFinalSquare;
		
		if(finishedMaze) {
			startPosition = new Vector2d(r.getPosition());
		}
		
		if(inForbiddenSquare)
			simulator.stopSimulation();
	}

	private void stepFinishedMaze(Simulator simulator) {
		//this is to prevent the robot from going to the forbidden squares
		checkSquares();
		
		wentBack = inRoom();
		
		if(wentBack || inForbiddenSquare)
			simulator.stopSimulation();
	}
	
	private double computeFitness() {
		
//		fitness = 0;
		
		if(!finishedRoom) { //Didn't clear the Cluttered Room
			fitness = computeFitnessUnfinishedRoom();
		} else if(!finishedMaze) { //Cleared the Cluttered Room
			fitness = 1;
			fitness+= computeFitnessFinishedRoom();
		} else if(!wentBack) { //Cleared the Maze
			fitness = 2;
			fitness+= computeFitnessFinishedMaze();
		} else { //Cleared everything
			fitness = 3;
//			fitness+=computeFitnessWentBack();
		}
		
//		if(r.isInvolvedInCollison())
//			fitness*=0.5;
		
		return fitness;
	}
	
	private double computeFitnessUnfinishedRoom() {
		double distanceToRoomFinish = getDistanceToRoomFinish();
		
		if(fitness > distanceToRoomFinish)
			distanceToRoomFinish = fitness;
		
		return distanceToRoomFinish;
	}
	
	private double computeFitnessFinishedRoom() {
		if(mazePenalty > 0)
			return mazePenalty*-1;
		return getProgress();
	}
	
	private double computeFitnessFinishedMaze() {
		if(mazePenalty > 0)
			return 0;
		return getDistanceToRoomFinish();
	}
	
	private double computeFitnessWentBack(Simulator simulator) {
		return (maxSteps-simulator.getTime())/maxSteps;
	}
	
	public double getDistanceToRoomFinish() {
		
		Vector2d start = startPosition;
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(clutteredEnv.getRoomExitPosition());
		exit.y-=clutteredEnv.getExitWidth();
		
		return (start.distanceTo(exit)-robot.distanceTo(exit))/start.distanceTo(exit);
	}
}	