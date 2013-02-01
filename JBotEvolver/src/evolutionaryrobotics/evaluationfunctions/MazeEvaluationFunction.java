package evolutionaryrobotics.evaluationfunctions;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;

import simulation.Simulator;
import simulation.environment.MazeEnvironment;
import simulation.environment.MazeEnvironment.Square;
import simulation.robot.Robot;

public class MazeEvaluationFunction  extends EvaluationFunction {

	private double fitness;
	private MazeEnvironment env;
	private boolean touchedWall = false;
	private double startDistance = 0;
	private int currentIndex = 0;
	private int stepLastIndex = 0;
	private int currentStep = 0;
	private int stepThreshold = 150;
	
	public MazeEvaluationFunction(Simulator simulator){
		super(simulator);
		this.env = (MazeEnvironment)simulator.getEnvironment();
		this.startDistance = env.getSquares().peek().getDistance();
	}
	
	@Override
	public double getFitness() {
		
		if(currentIndex == env.getSquares().size()-1)
			return 1;
		
		if(touchedWall)
			return fitness;

		return (startDistance-getDistanceToFinish())/startDistance;
	}
	
	private double getDistanceToFinish() {
		int nextIndex = currentIndex == (env.getSquares().size()-1) ? currentIndex : currentIndex + 1;
		
		Robot r = env.getRobots().get(0);
		double robotX = r.getPosition().getX();
		double robotY = r.getPosition().getY();
		double[] envCoords = env.envCoordToImageCoord(robotX, robotY);
		robotX = envCoords[0];
		robotY = envCoords[1];
			
		Square sq = env.getSquares().get(nextIndex);
		
		double distanceX = Math.abs(robotX-sq.getX());
		double distanceY = Math.abs(robotY-sq.getY());
		
		double directLine = Math.hypot(distanceX, distanceY);
		
		return sq.getDistance() + directLine;
	}

	@Override
	public void update(double time) {
		
		/*if(++currentStep-stepLastIndex>stepThreshold) {
			touchedWhite = true;
			fitness = (startDistance-getDistanceToFinish())/startDistance;
			simulator.getExperiment().endExperiment();
		}*/
		
		//System.out.println(getFitness());
		if(!touchedWall) {
			
			if(env.getRobots().get(0).isInvolvedInCollison()) {
				fitness = (startDistance-getDistanceToFinish())/startDistance;
				touchedWall = true;
				simulator.getExperiment().endExperiment();
			} else {
				//check if we are on top of a different square
				updateCurrentIndex();
			}
		}
	}
	
	private void updateCurrentIndex() {
		LinkedList<Square> squares = env.getSquares();
		
		Robot robot = env.getRobots().get(0);
		
		int i = 0;
		if(currentIndex > 0)
			i = currentIndex-1;
		
		//Search only previous, current and next squares
		//I think we can skip the current square, though
		for(; i < squares.size() && i <= currentIndex+1 ; i++) {
			Square s = squares.get(i);
			
			double robotX = robot.getPosition().getX();
			double robotY = robot.getPosition().getY();
			
			double[] envCoords = env.envCoordToImageCoord(robotX, robotY);
			robotX = envCoords[0];
			robotY = envCoords[1];
			
			int squareSize = env.getSquareSize();
			
			Rectangle squareRect = new Rectangle(s.getX()-squareSize/2,s.getY()-squareSize/2,squareSize,squareSize);
			Rectangle robotRect = new Rectangle((int)robotX-1,(int)robotY-1,2,2);
			
			if(squareRect.intersects(robotRect)) {
				currentIndex = i;
				//System.out.println(currentIndex);
				stepLastIndex = currentStep;
				if(i == squares.size()-1)
					simulator.getExperiment().endExperiment();
				break;
			}
		}
	}
}
