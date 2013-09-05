package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PreyAggregationEvaluationFunction extends EvaluationFunction {
	
	double robotPercentage;
	double preyPercentage;
	double robotDistance;
	double preyDistance;
	
	public PreyAggregationEvaluationFunction(Arguments args) {
		super(args);
		this.robotPercentage = args.getArgumentAsDoubleOrSetDefault("robotpercentage", 1);
		this.preyPercentage = args.getArgumentAsDoubleOrSetDefault("preypercentage", 1);
		
		this.preyDistance = args.getArgumentAsDoubleOrSetDefault("preydistance", 1);
		this.robotDistance = args.getArgumentAsDoubleOrSetDefault("robotdistance", 1);
	}

	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		
		double currentFitness = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			if(robots.get(i).isInvolvedInCollison())
				continue;
			
			//award being close to robots
			for(int j = i+1 ; j < robots.size() ; j++) {
				double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
				
				if(distance < robotDistance)
					currentFitness+= (robotDistance-distance)*robotPercentage;
			}
			
			//award being close to preys
			for(Prey p : simulator.getEnvironment().getPrey()) {
				double distance = robots.get(i).getPosition().distanceTo(p.getPosition());
				if(distance < preyDistance)
					currentFitness+= (preyDistance-distance)*preyPercentage;
			}
		}
		
		fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
	}
}