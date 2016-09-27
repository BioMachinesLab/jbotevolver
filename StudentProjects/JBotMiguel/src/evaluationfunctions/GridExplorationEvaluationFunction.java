package evaluationfunctions;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class GridExplorationEvaluationFunction extends EvaluationFunction {

	private boolean isSetup = false;
	private boolean[][] visited;
	private double resolution = 1;
	private double minimumDistance = 0;
	private double width,height;
	
	public GridExplorationEvaluationFunction(Arguments args) {
		super(args);
		resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
		minimumDistance = args.getArgumentAsDoubleOrSetDefault("minimumdistance", minimumDistance);
	}
	
	public void setup(Simulator simulator) {
		width = simulator.getEnvironment().getWidth();
		height = simulator.getEnvironment().getHeight();
		visited = new boolean[(int)(width/resolution)][(int)(height/resolution)];
	}

	@Override
	public void update(Simulator simulator) {
		
		if(!isSetup) {
			setup(simulator);
			isSetup = true;
		}
		
		ArrayList<Robot> robots = simulator.getRobots();
		
		if(minimumDistance == 0 && robots.size() > 0)
			minimumDistance = robots.size()*robots.get(0).getDiameter()*2;
		
		double maxDistance = 0;
		double centerX = 0;
		double centerY = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			if(robots.get(i).isInvolvedInCollison()) {
				maxDistance = 50;
			}
			
			for(int j = i+1 ; j < robots.size() ; j++) {
				double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
				
				if(distance > maxDistance)
					maxDistance = distance;
			}
			
			centerX+=robots.get(i).getPosition().getX();
			centerY+=robots.get(i).getPosition().getY();
		}
		
		centerX/=robots.size();
		centerY/=robots.size();
		
		if(maxDistance < minimumDistance) {
			centerX/=resolution;
			centerY/=resolution;
			int indexX = (int)(centerX + visited.length/2);
			if(indexX < visited.length && indexX >= 0) {
				int indexY = (int)(centerY + visited[indexX].length/2);
				if(indexY < visited[indexX].length && indexY >= 0 && !visited[indexX][indexY]) {
					visited[indexX][indexY] = true;
					fitness++;
				}
			}
		}
	}
	
	@Override
	public double getFitness() {
		return super.getFitness() / (visited.length*visited[0].length);
	}
}