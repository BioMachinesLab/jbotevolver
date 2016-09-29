package evaluationfunctions;

import java.util.ArrayList;

import controllers.BehaviorController;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsMultiPreyEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PreyAggregationExponentialEvaluationFunction extends EvaluationFunction {
	
	double robotDistance;
	double preyDistance;
	
	boolean debug = false;
	boolean debug2 = false;
	
	boolean onlyOnePrey = false;
	
	int preysCaught = 0;
	
	boolean killCollision = false;
	double punishCollision = 0;
	
	double penalty = 0;
	
	public PreyAggregationExponentialEvaluationFunction(Arguments args) {
		super(args);
		
		this.preyDistance = args.getArgumentAsDoubleOrSetDefault("preydistance", 1);
		this.robotDistance = args.getArgumentAsDoubleOrSetDefault("robotdistance", 1);
		this.onlyOnePrey = args.getArgumentAsIntOrSetDefault("onlyoneprey", 0) == 1;
		this.killCollision = args.getArgumentAsIntOrSetDefault("killcollision", 0) == 1;
		this.punishCollision = args.getArgumentAsDoubleOrSetDefault("punishcollision", punishCollision);
//		this.debug2 = args.getArgumentAsIntOrSetDefault("debug2", 0) == 1;
	}
	
	@Override
	public void update(Simulator simulator) {
		ArrayList<Robot> robots = simulator.getRobots();
		
		double currentFitness = 0;
		double maxSwitches = 0;
		
		for(int i = 0 ; i <  robots.size() ; i++) {
			
			if(robots.get(i).isInvolvedInCollison())
				penalty+=punishCollision;
			
			if(robots.get(i).getController() instanceof BehaviorController) {
				BehaviorController c = (BehaviorController)robots.get(i).getController();
				maxSwitches = c.getNumberOfSwitches() > maxSwitches ? c.getNumberOfSwitches() : maxSwitches;
			}
			
			double currentRobotFitness = 0;
			int nearRobots = 0;
			
			//award being close to robots
			for(int j = i+1 ; j < robots.size() ; j++) {
				double distance = robots.get(i).getPosition().distanceTo(robots.get(j).getPosition());
				
				if(distance < robotDistance) {
					nearRobots++;
					currentRobotFitness+= (robotDistance-distance);
				}
			}
			
//			if(debug)
				currentRobotFitness = 0;
			
			//award being close to preys
			for(Prey p : simulator.getEnvironment().getPrey()) {
				double distance = robots.get(i).getPosition().distanceTo(p.getPosition());
				if(distance < preyDistance) {
					currentRobotFitness+= (preyDistance-distance)/preyDistance;
				}
			}
			
//			if(debug2)
				currentRobotFitness*=nearRobots;
//			else if(closeToRobots && closeToPreys)
//				currentRobotFitness*=2;
				
			currentFitness+=currentRobotFitness;
		}

		fitness+= currentFitness/(double)simulator.getEnvironment().getSteps();
		
		if(maxSwitches > simulator.getEnvironment().getSteps()/50.0) {
			fitness = 0;
			simulator.stopSimulation();
		}
		
		if(simulator.getEnvironment() instanceof TwoRoomsMultiPreyEnvironment) {
			preysCaught = ((TwoRoomsMultiPreyEnvironment)simulator.getEnvironment()).getPreysCaught();
		}
		
		if(onlyOnePrey) {
			if(preysCaught > 0) {
				double percentageSpeed = 1-(simulator.getTime() / simulator.getEnvironment().getSteps());
				fitness = 1+percentageSpeed;
				simulator.stopSimulation();
			}
		}
	}
	
	@Override
	public double getFitness() {
		if(onlyOnePrey)
			return fitness;
		return fitness+preysCaught-penalty;
	}
}