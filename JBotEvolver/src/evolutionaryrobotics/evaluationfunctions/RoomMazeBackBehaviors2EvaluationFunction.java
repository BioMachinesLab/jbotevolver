package evolutionaryrobotics.evaluationfunctions;

import evolutionaryrobotics.neuralnetworks.BehaviorController;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.ClutteredMazeEnvironment;
import simulation.robot.sensors.NearRobotSensor;
import simulation.util.Arguments;

public class RoomMazeBackBehaviors2EvaluationFunction extends ClutteredMazeEvaluationFunction {
	
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
	
	private int transitionSteps = 50;
	private int currentTransitionSteps = 0;
	
	public RoomMazeBackBehaviors2EvaluationFunction(Arguments args) {
		super(args);
		totalTime = args.getArgumentAsDoubleOrSetDefault("totaltime", 2000);
		transitionSteps = args.getArgumentAsIntOrSetDefault("disabletransition", 0) == 0 ? 50 : 0;
	}

	@Override
	public void update(Simulator simulator) {
		currentTransitionSteps++;
		if(r == null)
			r = simulator.getEnvironment().getRobots().get(0);
		if(controller == null)
			controller = (BehaviorController)r.getController();
		if(clutteredEnv == null)
			clutteredEnv = (ClutteredMazeEnvironment)simulator.getEnvironment();
		if(env == null)
			env = (ClutteredMazeEnvironment)simulator.getEnvironment();

		if(r.isInvolvedInCollison())
			simulator.stopSimulation();
		
		if(!finishedRoom) { //Didn't clear the Cluttered Room
			stepUnfinishedRoom();
		} else if(!finishedMaze) { //Cleared the Cluttered Room
			stepFinishedRoom(simulator);
		} else if(!wentBack) { //Cleared the Maze
			stepFinishedMaze(simulator);
		} else { //Cleared everything
			simulator.stopSimulation();
		}
		
		computeFitness(simulator);
	}
	
	private void stepUnfinishedRoom() {
		
		if(controller.getCurrentSubNetwork() == 0)
			roomTicks++;
		
		roomTime++;
		
		finishedRoom = !inRoom();
		
		if(finishedRoom)
			currentTransitionSteps = 0;
	}
	
	private void stepFinishedRoom(Simulator simulator) {
		checkSquares();
		
		NearRobotSensor s = (NearRobotSensor)simulator.getRobots().get(0).getSensorByType(NearRobotSensor.class);
		
		if(controller.getCurrentSubNetwork() == 1 || (controller.getCurrentSubNetwork() == 0 && currentTransitionSteps <= transitionSteps))
			mazeTicks++;
		
		mazeTime++;

//		finishedMaze = inFinalSquare;
		
		if(s.getSensorReading(0) > 0) {
			startPosition = new Vector2d(r.getPosition());
			finishedMaze = true;
		}
		
		if(inForbiddenSquare)
			simulator.stopSimulation();
	}

	private void stepFinishedMaze(Simulator simulator) {
		//this is to prevent the robot from going to the forbidden squares
		
		if(controller.getCurrentSubNetwork() == 2)
			goBackTicks++;
		
		goBackTime++;
		
		checkSquares();
		
		wentBack = inRoom();
		
		if(wentBack || inForbiddenSquare)
			simulator.stopSimulation();
	}
	
	private void computeFitness(Simulator simulator) {
		
		fitness = 0;
		
		fitness+=computeFitnessUnfinishedRoom();
		fitness+=computeFitnessFinishedRoom();
		fitness+=computeFitnessFinishedMaze();
		
		if(wentBack)
			fitness+=(totalTime-simulator.getTime())/totalTime;
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
		Vector2d exit = new Vector2d(clutteredEnv.getRoomExitPosition());
		exit.y-=clutteredEnv.getExitWidth();
		
		return (start.distanceTo(exit)-robot.distanceTo(exit))/start.distanceTo(exit);
	}
}	