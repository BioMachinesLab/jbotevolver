package evaluationfunctions;

import controllers.BehaviorController;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsMultiPreyEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CFDerived2 extends PreyAggregationExponentialEvaluationFunction {
	
	private boolean penalize = false;
	private boolean sync = false;
	
	public CFDerived2(Arguments args) {
		super(args);
		penalize = args.getArgumentAsIntOrSetDefault("penalize", 0) == 1;
		sync = args.getArgumentAsIntOrSetDefault("sync", 0) == 1;
	}

	@Override
	public void update(Simulator simulator) {
		
//		super.update(simulator);
		
		Prey p = simulator.getEnvironment().getPrey().get(0);
		double x = p.getPosition().getX();
		
		double nRobots = simulator.getRobots().size();
		double steps = simulator.getEnvironment().getSteps();
		
		if(simulator.getEnvironment() instanceof TwoRoomsMultiPreyEnvironment) {
			preysCaught = ((TwoRoomsMultiPreyEnvironment)simulator.getEnvironment()).getPreysCaught();
		}
		
		boolean allSynced = true;
		double points = 0;
		
		for(Robot r : simulator.getRobots()) {
			BehaviorController b = (BehaviorController)r.getController();
			
			int currentSubNetwork = b.getCurrentSubNetwork();
			//0 open_cross
			//1 forage
			
			double rPos = r.getPosition().getX();
			
			if((rPos > 0.3 && x > 0.3) || (rPos < -0.3 && x < -0.3)) {
				if(currentSubNetwork == 1) {
					points+=0.5/nRobots/steps;
				}else {
					allSynced = false;
					if(penalize)
						points-=0.5/nRobots/steps;
				}
			} else {
				if(currentSubNetwork == 0)
					points+=0.5/nRobots/steps;
				else {
					allSynced = false;
					if(penalize)
						points-=0.5/nRobots/steps;
				}
			}
		}
		
		if(sync) {
			if(allSynced)
				fitness+=0.5/steps;
			else if(penalize)
				fitness-=0.5/steps;
		} else {
			fitness+=points;
		}
	}
}