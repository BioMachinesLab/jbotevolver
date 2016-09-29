package evaluationfunctions;

import controllers.BehaviorController;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsMultiPreyEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CrossForageDerivedEvaluationFunction extends EvaluationFunction{
	
	private TwoRoomsMultiPreyEnvironment env;
	private boolean penalize = false;
	private int penalizeRatio;
	private double reward = 0;

	public CrossForageDerivedEvaluationFunction(Arguments args) {
		super(args);
		penalize = args.getArgumentAsIntOrSetDefault("penalize", 1) == 1;
		penalizeRatio = args.getArgumentAsIntOrSetDefault("penalizeratio", 1);
	}

	@Override
	public void update(Simulator simulator) {
		
		if(env == null) {
			env = (TwoRoomsMultiPreyEnvironment)simulator.getEnvironment();
			reward = 1.0/(double)(env.getRobots().size()*env.getSteps());
		}
		
		Prey p = env.getPrey().get(0);
		double x = p.getPosition().getX();
		
		for(Robot r : simulator.getRobots()) {
			BehaviorController b = (BehaviorController)r.getController();
			
			int currentSubNetwork = b.getCurrentSubNetwork();
			//0 open_cross
			//1 forage
			
			double rPos = r.getPosition().getX();
			
			if((rPos > 0 && x > 0) || (rPos < 0 && x < 0)) {
				if(currentSubNetwork == 1)
					fitness+=reward;
				else if(penalize)
					fitness-=reward*penalizeRatio;
			} else {
				if(currentSubNetwork == 0)
					fitness+=reward;
				else if(penalize)
					fitness-=reward*penalizeRatio;
			}
		}
	}
	
	@Override
	public double getFitness() {
		if(env != null)
			return super.getFitness() +  env.getPreysCaught();
		else
			return super.getFitness();
	}
}