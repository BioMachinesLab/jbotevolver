package roommaze;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class TMazeEvaluationFunction  extends EvaluationFunction {

	protected TMazeEnvironment env;
	protected boolean touchedWall = false;
	protected boolean inForbiddenSquare = false;
	protected boolean inFinalSquare = false;
	protected double startDistance = 0;
	protected double maxSteps = 500;
	protected double progress = 0;
	private boolean dieOnForbidden = true;
	
	public TMazeEvaluationFunction(Arguments args){
		super(args);
		maxSteps = (double)(args.getArgumentAsIntOrSetDefault("maxsteps", (int)maxSteps));
		dieOnForbidden = args.getArgumentAsIntOrSetDefault("dieonforbidden",1) == 1;
	}
	
	@Override
	public void update(Simulator simulator) {
		if(env == null)
			env = (TMazeEnvironment)simulator.getEnvironment();
		startDistance = env.getSquares().peek().getDistance();
		
		if(env.killSample())
			simulator.stopSimulation();
		else if(env.getRobots().get(0).isInvolvedInCollison()) {
			touchedWall = true;
			simulator.stopSimulation();
		}else {
			checkSquares();
			if(inFinalSquare || inForbiddenSquare)
				simulator.stopSimulation();
		}
		
		progress = getProgress();
		
		if(inFinalSquare)
			fitness = 1 + (maxSteps-simulator.getTime())/maxSteps;
		else if(touchedWall || inForbiddenSquare)
			fitness = wallCollisionFitness();
		else
			fitness = progress;
	}

	protected void checkSquares() {
		
		Robot r = env.getRobots().get(0);
		
		Square sq = env.getSquares().peekLast();
		
		if(env.checkIntersection(sq, r))
			inFinalSquare = true;
		else if(dieOnForbidden)
			inForbiddenSquare = checkForbiddenSquares(r) != null;
		
	}
	
	protected double getProgress() {
		
		Robot r = env.getRobots().get(0);
		
		Square sq = getNextClosestSquare(r);
		
		double distanceToSquare = getDistanceToSquare(r, sq);
		double totalDistance = sq.getDistance() + distanceToSquare;
		
		double progress = (startDistance-totalDistance)/startDistance;
		
		return progress;
	}
	
	protected double wallCollisionFitness() {
		return getProgress()/3;
	}
	
	protected double getDistanceToSquare(Robot r, Square sq) {
		double distanceX = Math.abs(r.getPosition().getX()-sq.getX());
		double distanceY = Math.abs(r.getPosition().getY()-sq.getY());
		return Math.hypot(distanceX, distanceY);
	}
	
	protected Square checkForbiddenSquares(Robot r) {
		
		for(int i = 0 ; i < env.getForbiddenSquares().size() ; i++) {
			Square sq = env.getForbiddenSquares().get(i);
			if(env.checkIntersection(sq, r))
				return sq;
		}
		return null;
	}
	
	protected Square getNextClosestSquare(Robot r) {
		
		int closestIndex = 0;
		double closestDistance = Double.MAX_VALUE;
		
		for(int i = 0 ; i < env.getSquares().size() ; i++) {
			
			Square sq = env.getSquares().get(i);
			
			double distance = getDistanceToSquare(r, sq);
			
			if(distance < closestDistance) {
				closestDistance = distance;
				closestIndex = i;
			}
		}
		
		if(closestIndex < (env.getSquares().size() - 1))
			closestIndex++;
		
		return env.getSquares().get(closestIndex);
	}
	
	public boolean touchedWall() {
		return touchedWall;
	}
}