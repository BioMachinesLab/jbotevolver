package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.environment.TMazeEnvironment;
import simulation.environment.TMazeEnvironment.Square;
import simulation.robot.Robot;
import simulation.robot.actuators.BehaviorActuator;
import simulation.util.Arguments;

public class TMazeEvaluationFunction  extends EvaluationFunction {

	protected boolean touchedWall = false;
	protected boolean inForbiddenSquare = false;
	protected boolean inFinalSquare = false;
	
	protected BehaviorActuator behaviorActuator;
	
	protected TMazeEnvironment env;
	
	protected double startDistance = 0;
	protected double steps = 0;
	
	protected double maxSteps = 500;
	
	protected double progress = 0;
	
	private boolean dieOnForbidden = true;
	
	public TMazeEvaluationFunction(Simulator simulator, Arguments args){
		super(simulator);
		this.env = (TMazeEnvironment)simulator.getEnvironment();
		this.startDistance = env.getSquares().peek().getDistance();
		maxSteps = (double)(args.getArgumentAsIntOrSetDefault("maxsteps", (int)maxSteps));
		dieOnForbidden = args.getArgumentAsIntOrSetDefault("dieonforbidden",1) == 1;
	}
	
	@Override
	public double getFitness() {
		
		if(inFinalSquare)
			return 1 + (maxSteps-steps)/maxSteps;
		
		if(touchedWall || inForbiddenSquare)
			return wallCollisionFitness();

		return progress;
	}
	
	@Override
	public void step() {
		steps++;
		
		if(env.killSample())
			simulator.getExperiment().endExperiment();
		else if(env.getRobots().get(0).isInvolvedInCollison()) {
			touchedWall = true;
			simulator.getExperiment().endExperiment();
		}else {
			checkSquares();
			if(inFinalSquare || inForbiddenSquare)
				simulator.getExperiment().endExperiment();
		}
		
		progress = getProgress();
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
