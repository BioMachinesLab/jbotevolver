package evolutionaryrobotics.evaluationfunctions;

import evolutionaryrobotics.neuralnetworks.BehaviorController;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.util.Arguments;

public class RoomMazeBackBehaviorsEvaluationFunction extends ClutteredMazeEvaluationFunction {
	
	private boolean finishedRoom = false;
	private boolean finishedMaze = false;
	private boolean wentBack = false;
	private BehaviorController controller = null;
	
	private double roomTime = 0;
	private double roomTicks = 0;
	
	private double mazeTime = 0;
	private double mazeTicks = 0;
	
	private double goBackTime = 0;
	private double goBackTicks = 0;
	
	private double totalTime = 2000;
	
	public RoomMazeBackBehaviorsEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator,args);
		controller = (BehaviorController)simulator.getEnvironment().getRobots().get(0).getController();
		totalTime = args.getArgumentAsDoubleOrSetDefault("totaltime", 2000);
	}

	@Override
	public void update(double time) {

		if(r.isInvolvedInCollison())
			simulator.stopSimulation();
		
		if(!finishedRoom) { //Didn't clear the Cluttered Room
			stepUnfinishedRoom();
		} else if(!finishedMaze) { //Cleared the Cluttered Room
			stepFinishedRoom();
		} else if(!wentBack) { //Cleared the Maze
			stepFinishedMaze();
		} else { //Cleared everything
			simulator.stopSimulation();
		}
		
		steps++;
		
		computeFitness();
	}
	
	private void stepUnfinishedRoom() {
		
		if(controller.getCurrentSubNetwork() == 2)
			roomTicks++;
		
		roomTime++;
		
		finishedRoom = !inRoom();
	}
	
	private void stepFinishedRoom() {
		checkSquares();
		
		if(controller.getCurrentSubNetwork() == 1)
			mazeTicks++;
		
		mazeTime++;

		finishedMaze = inFinalSquare;
		
		if(finishedMaze) {
			startPosition = new Vector2d(r.getPosition());
		}
		
		if(inForbiddenSquare)
			simulator.stopSimulation();
	}

	private void stepFinishedMaze() {
		//this is to prevent the robot from going to the forbidden squares
		
		if(controller.getCurrentSubNetwork() == 0)
			goBackTicks++;
		
		goBackTime++;
		
		checkSquares();
		
		wentBack = inRoom();
		
		if(wentBack || inForbiddenSquare)
			simulator.stopSimulation();
	}
	
	private void computeFitness() {
		
		fitness = 0;
		
//		if(finishedRoom)
			fitness+=computeFitnessUnfinishedRoom();
//		if(finishedMaze)
			fitness+=computeFitnessFinishedRoom();
//		if(wentBack)
			fitness+=computeFitnessFinishedMaze();
		
		if(wentBack)
			fitness+=(totalTime-steps)/totalTime;
	}
	
	//solving room
	private double computeFitnessUnfinishedRoom() {
		if(roomTime == 0)
			return 0;
		return (roomTicks/roomTime);
	}
	
	//solving maze
	private double computeFitnessFinishedRoom() {
		if(mazeTime == 0)
			return 0;
		return (mazeTicks/mazeTime);
	}
	
	//going back
	private double computeFitnessFinishedMaze() {
		if(goBackTime == 0)
			return 0;
		return (goBackTicks/goBackTime);
	}

	public double getDistanceToRoomFinish() {
		
		Vector2d start = startPosition;
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(env.getRoomExitPosition());
		exit.y-=env.getExitWidth();
		
		return (start.distanceTo(exit)-robot.distanceTo(exit))/start.distanceTo(exit);
	}
}	