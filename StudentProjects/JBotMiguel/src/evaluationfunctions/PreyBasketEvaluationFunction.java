package evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environments.TwoRoomsBasketEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PreyBasketEvaluationFunction extends EvaluationFunction {
	
	private TwoRoomsBasketEnvironment env;
	private Vector2d startPrey;
	private Vector2d targetPrey;
	private double totalDistance = 0;
	private boolean onePrey = false;
	private int preyCaught = 0;
	private boolean penalty = false;
	private double penaltyValue = 0;
	private double totalPenalty = 0;
	private boolean neat = false;

	public PreyBasketEvaluationFunction(Arguments args) {
		super(args);
		onePrey = args.getArgumentAsIntOrSetDefault("oneprey", 1) == 1;
		penalty = args.getArgumentAsIntOrSetDefault("penalty", 0) == 1;
		neat = args.getArgumentAsIntOrSetDefault("neat", 0) == 1;
		penaltyValue = args.getArgumentAsIntOrSetDefault("penaltyvalue", 0);
	}

	@Override
	public void update(Simulator sim) {
		if(env == null) {
			setup(sim);
			if(penalty && penaltyValue == 0)
				penaltyValue = 1.0/sim.getRobots().size()/(double)sim.getEnvironment().getSteps();
		}
		if(penalty) {
			for(Robot r : sim.getEnvironment().getRobots()) {
				if(r.isInvolvedInCollison()) {
					totalPenalty+=penaltyValue;
				}
			}
		}
		
		Prey p = env.getPrey().get(0);
		
		double distance = p.getPosition().distanceTo(targetPrey);
		
		if(env.getPreysCaught() > 0 && onePrey) {
			sim.stopSimulation();
			fitness = 1 + (env.getSteps()-sim.getTime())/(double)env.getSteps() - totalPenalty;
		} else if (!onePrey){
			
			if(preyCaught != env.getPreysCaught()) {
				preyCaught = env.getPreysCaught();
				setup(sim);
			}
			fitness = (totalDistance-distance)/totalDistance - totalPenalty;
			
		} else {
			fitness = (totalDistance-distance)/totalDistance - totalPenalty;
		}
	}
	
	@Override
	public double getFitness() {
		if(onePrey) {
			return fitness;
		} else {
			double f = fitness + env.getPreysCaught();
			return neat ? Math.max(0,f) : f;
		}
	}
	
	private void setup(Simulator sim) {
		env = (TwoRoomsBasketEnvironment)sim.getEnvironment();
		startPrey = env.getPrey().get(0).getPosition();
		for(LightPole p : env.getBaskets()) {
			if(targetPrey == null) {
				targetPrey = new Vector2d(p.getPosition());
			} else if(p.getPosition().distanceTo(startPrey) < targetPrey.distanceTo(startPrey)) {
					targetPrey = new Vector2d(p.getPosition());
			}
		}
		totalDistance = startPrey.distanceTo(targetPrey);
	}
}
