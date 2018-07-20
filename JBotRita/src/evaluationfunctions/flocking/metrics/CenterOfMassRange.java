package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CenterOfMassRange extends EvaluationFunction  {
	
	protected Simulator simulator;
	@ArgumentsAnnotation(name = "cohensionDistance", defaultValue = "0.25")
	protected double cohensionDistance;
	
	public CenterOfMassRange(Arguments args) {
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
		double rewardOfSwarm=0;
		ArrayList<Robot> robots = simulator
				.getEnvironment().getRobots();

		for(int i = 0 ; i <  robots.size() ; i++) {

			Vector2d robotPosition=robots.get(i).getPosition();
			double centerOfMassX=0, centerOfMassY=0, numberOfNeighboursInRange=0;
			for(int j = 0 ; j < robots.size() ; j++) {  
				Vector2d neighbourPosition=robots.get(j).getPosition();

				if(robotPosition.distanceTo(neighbourPosition)<=cohensionDistance){
					
					if ( i!= j) {
						centerOfMassX+=neighbourPosition.x;
						centerOfMassY+=neighbourPosition.y;
						numberOfNeighboursInRange++;
					}
				}
			}
			Vector2d centerOfMass=new Vector2d(centerOfMassX/numberOfNeighboursInRange, centerOfMassY/numberOfNeighboursInRange);
			double distanceToCenterMass=robotPosition.distanceTo(centerOfMass);
			if(numberOfNeighboursInRange>=1){
				rewardOfSwarm+=1-distanceToCenterMass/cohensionDistance;
			}
		}
		currentFitness=rewardOfSwarm/ robots.size();
		fitness+= currentFitness;
	}
	
}
