package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CenterOfMassAll extends EvaluationFunction  {
	
	protected Simulator simulator;
	@ArgumentsAnnotation(name = "cohensionDistance", defaultValue = "0.25")
	protected double cohensionDistance;
	
	public CenterOfMassAll(Arguments args) {
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
			System.out.println("robot id="+ robots.get(i).getId());

			Vector2d robotPosition=robots.get(i).getPosition();
			double centerOfMassX=0;
			double centerOfMassY=0;
			double numberOfNeighboursInRange=0;
			for(int j = 0 ; j < robots.size() ; j++) {  
				Vector2d neighbourPosition=robots.get(j).getPosition();
				System.out.println("neughbout id="+ robots.get(i).getId());

				if(robotPosition.distanceTo(neighbourPosition)<=cohensionDistance){
					if ( i!= j) {
						System.out.println("eles vêm-se");
						System.out.println("distancia entre eles"+ robotPosition.distanceTo(neighbourPosition));
						centerOfMassX+=neighbourPosition.x;
						centerOfMassY+=neighbourPosition.y;
						System.out.println("centerX"+ neighbourPosition.x);
						System.out.println("centerY"+ neighbourPosition.y);
						System.out.println("Sum of"+centerOfMassX);


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
		currentFitness=rewardOfSwarm/(double) robots.size();
		fitness+= currentFitness;
	}
	
}