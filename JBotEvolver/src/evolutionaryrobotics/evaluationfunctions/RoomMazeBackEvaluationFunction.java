package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.util.Arguments;

public class RoomMazeBackEvaluationFunction extends ClutteredMazeEvaluationFunction {
	
	private boolean finishedRoom = false;
	private boolean finishedMaze = false;
	private boolean wentBack = false;
	private boolean firstStep = true;
	
	private double mazePenalty = 0;

	public RoomMazeBackEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator,args);
		mazePenalty = args.getArgumentAsDoubleOrSetDefault("mazepenalty", 0);
	}

	@Override
	public void step() {
		
		if(env.isMixed() && firstStep) {
			int sample = env.getMixedSample();
			if(sample < 4) {//inverse
				startPosition = new Vector2d(r.getPosition());
			} else if (sample < 4*2) {//normal tmaze
				//nothing
			} else if(sample < 4*3) {//room
				startPosition = new Vector2d(0,0);
			}
			firstStep = false;
		}
		
		if(env.isMixed())
			stepMixed();
		else {
			if(r.isInvolvedInCollison())
				simulator.getExperiment().endExperiment();
			
			if(!finishedRoom) { //Didn't clear the Cluttered Room
				stepUnfinishedRoom();
			} else if(!finishedMaze) { //Cleared the Cluttered Room
				stepFinishedRoom();
			} else if(!wentBack) { //Cleared the Maze
				stepFinishedMaze();
			} else { //Cleared everything
				simulator.getExperiment().endExperiment();
			}
		}
		
		steps++;
		
		this.fitness = computeFitness();
	}
	
	private void stepMixed() {
		int sample = env.getMixedSample();
		
		if(sample < 4) {//inverse
			stepFinishedMaze();
			if(wentBack)
				simulator.getExperiment().endExperiment();
		} else if (sample < 4*2) {//normal tmaze
			stepFinishedRoom();
			if(finishedMaze)
				simulator.getExperiment().endExperiment();
		} else if(sample < 4*3) {//room
			stepUnfinishedRoom();
			if(finishedRoom)
				simulator.getExperiment().endExperiment();
		}
	}
	
	private void stepUnfinishedRoom() {
		if(!inRoom())
			finishedRoom = true;
	}
	
	private void stepFinishedRoom() {
		checkSquares();

		finishedMaze = inFinalSquare;
		
		if(finishedMaze) {
			startPosition = new Vector2d(r.getPosition());
		}
		
		if(inForbiddenSquare)
			simulator.getExperiment().endExperiment();
	}

	private void stepFinishedMaze() {
		//this is to prevent the robot from going to the forbidden squares
		checkSquares();
		
		wentBack = inRoom();
		
		if(wentBack || inForbiddenSquare)
			simulator.getExperiment().endExperiment();
	}
	
	private double computeFitness() {
		
//		fitness = 0;
		
		if(env.isMixed())
			return computeMixedFitness();
		
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
	
	private double computeMixedFitness() {
		
		int sample = env.getMixedSample();
		
		if(sample < 4) {//inverse
			fitness = getDistanceToRoomFinish();
			if(fitness > 0.95)
				fitness = 2;
		} else if (sample < 4*2) {//normal tmaze
			fitness = getProgress();
			if(finishedMaze)
				fitness = 2;
		} else if(sample < 4*3) {//room
			fitness = computeFitnessUnfinishedRoom();
			if(finishedRoom)
				fitness = 2;
		}
		
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
	
	private double computeFitnessWentBack() {
		return (maxSteps-steps)/maxSteps;
	}

	@Override
	public double getFitness() {
		return fitness;
	}
	
	public double getDistanceToRoomFinish() {
		
		Vector2d start = startPosition;
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(env.getRoomExitPosition());
		exit.y-=env.getExitWidth();
		
		return (start.distanceTo(exit)-robot.distanceTo(exit))/start.distanceTo(exit);
	}
}	