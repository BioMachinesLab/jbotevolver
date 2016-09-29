package evaluationfunctions;

import mathutils.Vector2d;
import roommaze.ClutteredMazeEnvironment;
import roommaze.Square;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.NearRobotSensor;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SingleANNClutteredMaze extends EvaluationFunction {
	
	private int roomTime = 0;
	private int mazeTime = 0;
	private int returnTime = 0;
	
	private double startDistance = 0;
	
	private double roomWeight = 0;
	private double mazeWeight = 0;
	private double returnWeight = 0;
	
	private boolean finishedRoom = false;
	private boolean finishedMaze = false;
	private boolean wentBack = false;
	
	private boolean dieOnForbidden = false;
	private boolean inForbiddenSquare = false;
	
	private Vector2d closestRoomPoint;
	
	private ClutteredMazeEnvironment clutteredEnv;
	private Robot r;
	
	private double bestMazeProgress = 0;
	private double bestReturnProgress = 0;
	
	private boolean postEval = false;
	private boolean dieOnCollision = false;
	
	public SingleANNClutteredMaze(Arguments arguments) {
		super(arguments);
		roomWeight = arguments.getArgumentAsDoubleOrSetDefault("room",1);
		mazeWeight = arguments.getArgumentAsDoubleOrSetDefault("maze",1);
		returnWeight = arguments.getArgumentAsDoubleOrSetDefault("return",1);
		dieOnForbidden = arguments.getArgumentAsIntOrSetDefault("dieonforbidden",1) == 1;
		postEval = arguments.getArgumentAsIntOrSetDefault("posteval",0) == 1;
		dieOnCollision = arguments.getArgumentAsIntOrSetDefault("die",0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
		if(clutteredEnv == null) {
			clutteredEnv = (ClutteredMazeEnvironment)simulator.getEnvironment();
			startDistance = clutteredEnv.getSquares().peek().getDistance();
		}
		if(r == null)
			r = simulator.getEnvironment().getRobots().get(0);
		
		if(!finishedRoom) { //Didn't clear the Cluttered Room
			stepRoom(simulator);
		} else if(!finishedMaze) { //Cleared the Cluttered Room
			stepMaze(simulator);
		} else if(!wentBack) { //Cleared the Maze
			stepReturn(simulator);
		} else { //Cleared everything
			simulator.stopSimulation();
		}
		
		if(dieOnCollision && r.isInvolvedInCollison())
			simulator.stopSimulation();
		
		//if(simulator.simulationFinished())
			computeFitness(simulator);
	}
	
	private void computeFitness(Simulator simulator) {
		
		fitness = 1;
		
		fitness+=computeFitnessRoom();
		fitness+=computeFitnessMaze();
		fitness+=computeFitnessReturn();
		
	}
	
	private double computeFitnessRoom() {
		if(roomTime == 0)
			return 0;
		
		double f = 0;
		
		if(!finishedRoom) {
			double d = closestRoomPoint.distanceTo(clutteredEnv.getRoomExitPosition());
			double D = clutteredEnv.getRoomExitPosition().distanceTo(new Vector2d(0,0));
			f = (D-d)/D;
		} else {
			f = roomWeight + (1000.0 - roomTime)/1000.0;
		}
		
		if(postEval)
			f = finishedRoom ? 1 : 0;
		
		return f;
	}
	
	private double computeFitnessMaze() {
		if(mazeTime == 0)
			return 0;
	
		double f = 0;
		
		if(!finishedMaze) {
			f = bestMazeProgress;
		} else {
			f = mazeWeight + (1000.0 - mazeTime)/1000.0;
			
			if(r.isInvolvedInCollison() || inForbiddenSquare)
				f/=3;
		}
		
		if(postEval)
			f = finishedMaze ? 1 : 0;
		
		return f;
	}
	
	private double computeFitnessReturn() {
		if(returnTime == 0)
			return 0;
		
		double f = 0;
		
		if(!wentBack) {
			f = bestReturnProgress;
		} else {
			f = returnWeight + (1000.0 - returnTime)/1000.0;
			
			if(r.isInvolvedInCollison() || inForbiddenSquare)
				f/=3;
		}
		
		if(postEval)
			f = wentBack ? 1 : 0;
		
		return f;
	}
	
	private void stepRoom(Simulator simulator) {
		roomTime++;
		
		if(closestRoomPoint == null)
			closestRoomPoint = r.getPosition();
		
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(clutteredEnv.getRoomExitPosition());
		
		if(exit.distanceTo(robot) < exit.distanceTo(closestRoomPoint))
			closestRoomPoint = new Vector2d(robot);
		
		if(simulator.getTime() > 1000)
			simulator.stopSimulation();
		
		finishedRoom = !inRoom();
	}
	
	private void stepMaze(Simulator simulator) {
		mazeTime++;
		checkSquares();
		
		double progress = getProgress();
		
		if(progress > bestMazeProgress)
			bestMazeProgress = progress;
		
		NearRobotSensor s = (NearRobotSensor)simulator.getRobots().get(0).getSensorByType(NearRobotSensor.class);
		
		if(s.getSensorReading(0) > 0) {
			finishedMaze = true;
		}
		
		if(simulator.getTime()-roomTime > 1000) {
			simulator.stopSimulation();
			bestMazeProgress = 0;
		}
		
		if(inForbiddenSquare)
			simulator.stopSimulation();
	}

	private void stepReturn(Simulator simulator) {
		returnTime++;
		checkSquares();
		
		double progress = 1 - getProgress();
		
		if(progress > bestReturnProgress)
			bestReturnProgress = progress;
		
		wentBack = inRoom();
		
		if(simulator.getTime()-mazeTime-roomTime > 1000) {
			simulator.stopSimulation();
			bestReturnProgress = 0;
		}
		
		if(wentBack || inForbiddenSquare)
			simulator.stopSimulation();
	}
	
	public boolean inRoom() {
		
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(clutteredEnv.getRoomExitPosition());
		exit.y+=clutteredEnv.getExitWidth();
		
		return robot.y < exit.y;
	}
	
	protected void checkSquares() {
		
		Robot r = clutteredEnv.getRobots().get(0);
		
		if(dieOnForbidden)
			inForbiddenSquare = checkForbiddenSquares(r) != null;
		
	}
	
	protected double getProgress() {
		
		Robot r = clutteredEnv.getRobots().get(0);
		
		Square sq = getNextClosestSquare(r);
		
		double distanceToSquare = getDistanceToSquare(r, sq);
		double totalDistance = sq.getDistance() + distanceToSquare;
		
		double progress = (startDistance-totalDistance)/startDistance;
		
		return progress;
	}
	
	protected double getDistanceToSquare(Robot r, Square sq) {
		double distanceX = Math.abs(r.getPosition().getX()-sq.getX());
		double distanceY = Math.abs(r.getPosition().getY()-sq.getY());
		return Math.hypot(distanceX, distanceY);
	}
	
	protected Square checkForbiddenSquares(Robot r) {
		
		for(int i = 0 ; i < clutteredEnv.getForbiddenSquares().size() ; i++) {
			Square sq = clutteredEnv.getForbiddenSquares().get(i);
			if(clutteredEnv.checkIntersection(sq, r))
				return sq;
		}
		return null;
	}
	
	protected Square getNextClosestSquare(Robot r) {
		
		int closestIndex = 0;
		double closestDistance = Double.MAX_VALUE;
		
		for(int i = 0 ; i < clutteredEnv.getSquares().size() ; i++) {
			
			Square sq = clutteredEnv.getSquares().get(i);
			
			double distance = getDistanceToSquare(r, sq);
			
			if(distance < closestDistance) {
				closestDistance = distance;
				closestIndex = i;
			}
		}
		
		if(closestIndex < (clutteredEnv.getSquares().size() - 1))
			closestIndex++;
		
		return clutteredEnv.getSquares().get(closestIndex);
	}

}
