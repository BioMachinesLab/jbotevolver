package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class AlignmentLocally extends EvaluationFunction {

	protected Simulator simulator;
	
	@ArgumentsAnnotation(name = "cohensionDistance", defaultValue = "0.25")
	protected double cohensionDistance;
	
	public AlignmentLocally(Arguments args) {
		super(args);	
		cohensionDistance = args.getArgumentIsDefined("cohensionDistance") ? args
				.getArgumentAsDouble("cohensionDistance") : 0.25;	
	}
	
	@Override
	public double getFitness() {
		return fitness/simulator.getTime();
	}
	
	

	@Override
	public void update(Simulator simulator) {	
		this.simulator=simulator;
		ArrayList<Robot> robots = simulator
				.getEnvironment().getRobots();
		
		double rewardOfSwarm=0;
		for(int i = 0 ; i <  robots.size() ; i++) {

			Vector2d robotPosition=robots.get(i).getPosition();
			double cos=0;
			double sen=0;
			double numberOfNeighboursInRange=0;
			
			for(int j = 0 ; j < robots.size() ; j++) {  
				Robot neighbour=robots.get(j);

				if(robotPosition.distanceTo(neighbour.getPosition())<=cohensionDistance){
					double angleOfRobot=neighbour.getOrientation();
					cos+=Math.cos(angleOfRobot);  
					sen+=Math.sin(angleOfRobot);
					numberOfNeighboursInRange++;
					
				}
			}
			if(numberOfNeighboursInRange>1){
				rewardOfSwarm+=Math.sqrt(cos*cos+ sen*sen)/numberOfNeighboursInRange;
			}
		}
		
		
		currentFitness=rewardOfSwarm/ robots.size();
		fitness+= currentFitness;

	}
	
	

}