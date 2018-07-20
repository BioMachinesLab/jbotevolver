package evaluationfunctions.flocking.metrics;

import java.util.ArrayList;
import mathutils.Vector2d;
import sensors.RobotSensorWithSources;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CenterOfMassPerceived extends EvaluationFunction  {
	
	protected Simulator simulator;
	@ArgumentsAnnotation(name = "cohensionDistance", defaultValue = "0.25")
	protected double cohensionDistance;
	
	public CenterOfMassPerceived(Arguments args) {
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
			PhysicalObject [] neighboursPerceived=(((RobotSensorWithSources)robots.get(i).getSensorByType(RobotSensorWithSources.class)).getSourcesPerceived());;

			double centerOfMassX=0, centerOfMassY=0,numberOfNeighboursPerceived=0;
			
			for(int j = 0 ; j < robots.size() ; j++) {  
				
				if(contains(neighboursPerceived,robots.get(j))){
					Vector2d neighbourPosition=robots.get(j).getPosition();
					centerOfMassX+=neighbourPosition.x;
					centerOfMassY+=neighbourPosition.y;
					numberOfNeighboursPerceived++;
				}
				
			}
			
			Vector2d centerOfMass=new Vector2d(centerOfMassX/numberOfNeighboursPerceived, centerOfMassY/numberOfNeighboursPerceived);
			double distanceToCenterMass=robotPosition.distanceTo(centerOfMass);
			if(numberOfNeighboursPerceived>=1){
				rewardOfSwarm+=1-distanceToCenterMass/cohensionDistance;
			}
		}
		currentFitness=rewardOfSwarm/ robots.size();
		fitness+= currentFitness;
	}
	
	
	private boolean contains(PhysicalObject [] neighboursPerceived, Robot neighbour){
		for (PhysicalObject neighbourPerceived: neighboursPerceived){
			if(neighbourPerceived!=null && neighbourPerceived.equals(neighbour)){
				return true;
			}
		}
		return false;
	}
	
}