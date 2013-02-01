package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.ClutteredMazeEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ClutteredMazeEvaluationFunction extends TMazeEvaluationFunction {
	
	protected Robot r;
	protected ClutteredMazeEnvironment env;
	protected Vector2d startPosition = new Vector2d();
	protected boolean finishedRoom = false;
	protected double fitness = 0;
	private boolean onlyRoom = false;
	private double closestDistance = 0;

	public ClutteredMazeEvaluationFunction(Simulator simulator, Arguments args) {
		super(simulator,args);
		r = simulator.getEnvironment().getRobots().get(0);
		env = (ClutteredMazeEnvironment)simulator.getEnvironment();
		startPosition.x = 0;
		startPosition.y = 0;
		onlyRoom = args.getArgumentIsDefined("onlyroom");
	}
	
	public boolean reachedRoomFinish() {
		return getDistanceToRoomFinish() > 0.95;
	}

	@Override
	public void update(double time) {
		if(!finishedRoom && reachedRoomFinish()) {
			finishedRoom = true;
			if(onlyRoom)
				simulator.getExperiment().endExperiment();
		} else if(r.isInvolvedInCollison()) {
			touchedWall = true;
			simulator.getExperiment().endExperiment();
		} else if(finishedRoom) {
			checkSquares();
			if(inFinalSquare || inForbiddenSquare)
				simulator.getExperiment().endExperiment();
		}
		
		steps++;
		
		this.fitness = computeFitness();
	}
	
	private double computeFitness() {
		
		double fitness = getDistanceToRoomFinish();
		
		if(fitness > 0.95)
			fitness = 1;
		
		if(fitness > closestDistance)
			closestDistance = fitness;
		
		fitness = closestDistance;
		
		if(finishedRoom) {
			
			fitness = (5.0 + (maxSteps-steps)/maxSteps)*fitness;
			if(closestDistance < fitness)
				closestDistance = fitness;
			
			fitness = closestDistance;
			
			if(inFinalSquare)
				fitness+= 1 + (maxSteps-steps)/maxSteps;
			
			if(touchedWall || inForbiddenSquare)
				fitness+= wallCollisionFitness();
			
		} else {
			if(r.isInvolvedInCollison())
				fitness*=0.5;			
		}
		
		return fitness;
	}

	@Override
	public double getFitness() {
		return fitness;
	}
	
	public boolean inRoom() {
		
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(env.getRoomExitPosition());
		exit.y+=env.getExitWidth();
		
		return robot.y < exit.y;
	}
	
	public double getDistanceToRoomFinish() {
		
		Vector2d start = startPosition;
		Vector2d robot = r.getPosition();
		Vector2d exit = new Vector2d(env.getRoomExitPosition());
		exit.y+=env.getExitWidth();
		
		return (start.distanceTo(exit)-robot.distanceTo(exit))/start.distanceTo(exit);
	}

}
